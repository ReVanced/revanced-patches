package app.revanced.patches.youtube.misc.playertype

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.fingerprint.PlayerTypeFingerprint
import app.revanced.patches.youtube.misc.playertype.fingerprint.VideoStateFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    description = "Hook to get the current player type and video playback state.",
    dependencies = [IntegrationsPatch::class]
)
object PlayerTypeHookPatch : BytecodePatch(
    setOf(PlayerTypeFingerprint, VideoStateFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/PlayerTypeHookPatch;"

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
}
