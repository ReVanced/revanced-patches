package app.revanced.patches.tumblr.timelinefilter

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tumblr.timelinefilter.fingerprints.*
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

/**
 * Add a filter to hide the given timeline object type.
 * The list of all Timeline object types is found in the TimelineObjectType class,
 * where they are mapped from their api name (returned by tumblr via the HTTP API) to the enum value name.
 *
 * @param typeName The enum name of the timeline object type to hide.
 */
@Suppress("KDocUnresolvedReference")
lateinit var addObjectTypeFilter: (typeName: String) -> Unit

@Suppress("unused")
val timelineFilterPatch = bytecodePatch(
    description = "Filter timeline objects.",
    requiresIntegrations = true,
) {
    val timelineConstructorResult by timelineConstructorFingerprint
    val timelineFilterIntegrationResult by timelineConstructorFingerprint
    val postsResponseConstructorResult by postsResponseConstructorFingerprint

    execute {
        timelineFilterIntegrationResult.let { integration ->
            val filterInsertIndex = integration.scanResult.patternScanResult!!.startIndex

            integration.mutableMethod.apply {
                val addInstruction = getInstruction<BuilderInstruction35c>(filterInsertIndex + 1)

                val filterListRegister = addInstruction.registerC
                val stringRegister = addInstruction.registerD

                // Remove "BLOCKED_OBJECT_DUMMY"
                removeInstructions(filterInsertIndex, 2)

                addObjectTypeFilter = { typeName ->
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
        }
        mapOf(
            timelineConstructorResult to 1,
            postsResponseConstructorResult to 2,
        ).forEach { (result, timelineObjectsRegister) ->
            result.mutableMethod.addInstructions(
                0,
                "invoke-static {p$timelineObjectsRegister}, " +
                    "Lapp/revanced/integrations/tumblr/patches/TimelineFilterPatch;->" +
                    "filterTimeline(Ljava/util/List;)V",
            )
        }
    }
}
