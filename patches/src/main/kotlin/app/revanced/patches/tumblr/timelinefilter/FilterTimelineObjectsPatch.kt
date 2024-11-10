package app.revanced.patches.tumblr.timelinefilter

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tumblr.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

/**
 * Add a filter to hide the given timeline object type.
 * The list of all Timeline object types is found in the TimelineObjectType class,
 * where they are mapped from their api name (returned by tumblr via the HTTP API) to the enum value name.
 *
 * @param typeName The enum name of the timeline object type to hide.
 */
@Suppress("KDocUnresolvedReference")
lateinit var addTimelineObjectTypeFilter: (typeName: String) -> Unit

val filterTimelineObjectsPatch = bytecodePatch(
    description = "Filter timeline objects.",
) {
    dependsOn(sharedExtensionPatch)

    execute {
        val filterInsertIndex = timelineFilterExtensionFingerprint.patternMatch!!.startIndex

        timelineFilterExtensionFingerprint.method.apply {
            val addInstruction = getInstruction<BuilderInstruction35c>(filterInsertIndex + 1)

            val filterListRegister = addInstruction.registerC
            val stringRegister = addInstruction.registerD

            // Remove "BLOCKED_OBJECT_DUMMY"
            removeInstructions(filterInsertIndex, 2)

            addTimelineObjectTypeFilter = { typeName ->
                // blockedObjectTypes.add({typeName})
                addInstructionsWithLabels(
                    filterInsertIndex,
                    """
                        const-string v$stringRegister, "$typeName"
                        invoke-virtual { v$filterListRegister, v$stringRegister }, Ljava/util/HashSet;->add(Ljava/lang/Object;)Z
                    """,
                )
            }
        }

        mapOf(
            timelineConstructorFingerprint to 1,
            postsResponseConstructorFingerprint to 2,
        ).forEach { (fingerprint, timelineObjectsRegister) ->
            fingerprint.method.addInstructions(
                0,
                "invoke-static {p$timelineObjectsRegister}, " +
                    "Lapp/revanced/extension/tumblr/patches/TimelineFilterPatch;->" +
                    "filterTimeline(Ljava/util/List;)V",
            )
        }
    }
}
