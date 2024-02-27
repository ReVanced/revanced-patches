package app.revanced.patches.youtube.overlaybutton.download.pip

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.overlaybutton.download.pip.fingerprints.PiPPlaybackFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.INTEGRATIONS_PATH
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Temporarily disable PiP when the external download button is clicked.
 * This patch works independently of the MinimizedPlayback patch.
 */
object DisablePiPPatch : BytecodePatch(
    setOf(PiPPlaybackFingerprint)
) {
    private const val INTEGRATIONS_VIDEO_HELPER_CLASS_DESCRIPTOR =
        "$INTEGRATIONS_PATH/utils/VideoHelpers;"

    override fun execute(context: BytecodeContext) {
        PiPPlaybackFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$insertRegister}, $INTEGRATIONS_VIDEO_HELPER_CLASS_DESCRIPTOR->isPiPAvailable(Z)Z
                        move-result v$insertRegister
                        """
                )
            }
        } ?: throw PiPPlaybackFingerprint.exception

    }
}
