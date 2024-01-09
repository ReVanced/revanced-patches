package app.revanced.patches.tumblr.timelinefilter

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.timelinefilter.fingerprints.PostsResponseConstructorFingerprint
import app.revanced.patches.tumblr.timelinefilter.fingerprints.TimelineConstructorFingerprint
import app.revanced.patches.tumblr.timelinefilter.fingerprints.TimelineFilterIntegrationFingerprint
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

@Patch(description = "Filter timeline objects.", requiresIntegrations = true)
object TimelineFilterPatch : BytecodePatch(
    setOf(TimelineConstructorFingerprint, TimelineFilterIntegrationFingerprint, PostsResponseConstructorFingerprint)
) {
    /**
     * Add a filter to hide the given timeline object type.
     * The list of all Timeline object types is found in the TimelineObjectType class,
     * where they are mapped from their api name (returned by tumblr via the HTTP API) to the enum value name.
     *
     * @param typeName The enum name of the timeline object type to hide.
     */
    @Suppress("KDocUnresolvedReference")
    internal lateinit var addObjectTypeFilter: (typeName: String) -> Unit private set

    override fun execute(context: BytecodeContext) {
        TimelineFilterIntegrationFingerprint.result?.let { integration ->
            val filterInsertIndex = integration.scanResult.patternScanResult!!.startIndex

            integration.mutableMethod.apply {
                val addInstruction = getInstruction<BuilderInstruction35c>(filterInsertIndex + 1)
                if (addInstruction.registerCount != 2) throw TimelineFilterIntegrationFingerprint.exception

                val filterListRegister = addInstruction.registerC
                val stringRegister = addInstruction.registerD

                // Remove "BLOCKED_OBJECT_DUMMY"
                removeInstructions(filterInsertIndex, 2)

                addObjectTypeFilter = { typeName ->
                    // blockedObjectTypes.add({typeName})
                    addInstructionsWithLabels(
                        filterInsertIndex, """
                            const-string v$stringRegister, "$typeName"
                            invoke-virtual { v$filterListRegister, v$stringRegister }, Ljava/util/HashSet;->add(Ljava/lang/Object;)Z
                        """
                    )
                }
            }
        } ?: throw TimelineFilterIntegrationFingerprint.exception

        mapOf(
            TimelineConstructorFingerprint to 1,
            PostsResponseConstructorFingerprint to 2
        ).forEach { (fingerprint, timelineObjectsRegister) ->
            fingerprint.result?.mutableMethod?.addInstructions(
                0,
                "invoke-static {p$timelineObjectsRegister}, " +
                        "Lapp/revanced/integrations/tumblr/patches/TimelineFilterPatch;->" +
                        "filterTimeline(Ljava/util/List;)V"
            ) ?: throw fingerprint.exception
        }
    }
}
