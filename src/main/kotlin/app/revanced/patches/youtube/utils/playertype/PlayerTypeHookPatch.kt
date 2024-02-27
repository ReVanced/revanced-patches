package app.revanced.patches.youtube.utils.playertype

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.fingerprints.YouTubeControlsOverlayFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.playertype.fingerprint.PlayerTypeFingerprint
import app.revanced.patches.youtube.utils.playertype.fingerprint.VideoStateFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(dependencies = [SharedResourceIdPatch::class])
object PlayerTypeHookPatch : BytecodePatch(
    setOf(
        PlayerTypeFingerprint,
        YouTubeControlsOverlayFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        PlayerTypeFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstruction(
                    0,
                    "invoke-static {p1}, $INTEGRATIONS_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V"
                )
            }
        } ?: throw PlayerTypeFingerprint.exception

        YouTubeControlsOverlayFingerprint.result?.let { parentResult ->
            VideoStateFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val endIndex = it.scanResult.patternScanResult!!.endIndex
                    val videoStateFieldName =
                        getInstruction<ReferenceInstruction>(endIndex).reference
                    addInstructions(
                        0, """
                        iget-object v0, p1, $videoStateFieldName  # copy VideoState parameter field
                        invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoState(Ljava/lang/Enum;)V
                        """
                    )
                }
            } ?: throw VideoStateFingerprint.exception
        } ?: throw YouTubeControlsOverlayFingerprint.exception

    }

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$UTILS_PATH/PlayerTypeHookPatch;"
}
