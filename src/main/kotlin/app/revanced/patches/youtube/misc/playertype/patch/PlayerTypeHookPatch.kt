package app.revanced.patches.youtube.misc.playertype.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.annotation.PlayerTypeHookCompatibility
import app.revanced.patches.youtube.misc.playertype.fingerprint.PlayerTypeFingerprint
import app.revanced.patches.youtube.misc.playertype.fingerprint.VideoStateFingerprint
import org.jf.dexlib2.iface.instruction.ReferenceInstruction

@Name("player-type-hook")
@Description("Hook to get the current player type and video playback state.")
@PlayerTypeHookCompatibility
@Version("0.0.1")
@DependsOn([IntegrationsPatch::class])
class PlayerTypeHookPatch : BytecodePatch(
    listOf(PlayerTypeFingerprint, VideoStateFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {

        PlayerTypeFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstruction(
                    0,
                    "invoke-static {p1}, $INTEGRATIONS_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V"
                )
            }
        } ?: return PlayerTypeFingerprint.toErrorResult()

        VideoStateFingerprint.result?.let {
            it.mutableMethod.apply {
                val endIndex = it.scanResult.patternScanResult!!.endIndex
                val videoStateFieldName = getInstruction<ReferenceInstruction>(endIndex).reference
                addInstructions(
                    0,
                    """
                        iget-object v0, p1, $videoStateFieldName  # copy VideoState parameter field
                        invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoState(Ljava/lang/Enum;)V
                    """
                )
            }
        } ?: return VideoStateFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    companion object {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/PlayerTypeHookPatch;"
    }

}
