package app.revanced.patches.tumblr.timelinefilter

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.removeInstructions
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

    apply {
        val filterInsertIndex = timelineFilterExtensionMethod.instructionMatches.first().index

        timelineFilterExtensionMethod.apply {
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

        arrayOf(
            timelineConstructorMethod to 1,
            postsResponseConstructorMethod to 2,
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
