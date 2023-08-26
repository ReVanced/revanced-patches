package app.revanced.patches.youtube.misc.playertype.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.fingerprint.PlayerTypeFingerprint
import app.revanced.patches.youtube.misc.playertype.fingerprint.VideoStateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Name("Player type hook")
@Description("Hook to get the current player type and video playback state.")
@DependsOn([IntegrationsPatch::class])
class PlayerTypeHookPatch : BytecodePatch(
    listOf(PlayerTypeFingerprint, VideoStateFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        PlayerTypeFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "invoke-static {p1}, $INTEGRATIONS_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V"
        ) ?: throw PlayerTypeFingerprint.exception

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
        } ?: throw VideoStateFingerprint.exception
    }

    companion object {
        private const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/PlayerTypeHookPatch;"
    }

}
