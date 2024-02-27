package app.revanced.patches.youtube.utils.controlsoverlay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.utils.controlsoverlay.fingerprints.ControlsOverlayConfigFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

object DisableControlsOverlayConfigPatch : BytecodePatch(
    setOf(ControlsOverlayConfigFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Added in YouTube v18.39.41
         *
         * No exception even if fail to resolve fingerprints.
         * For compatibility with YouTube v18.25.40 ~ YouTube v18.38.44.
         */
        ControlsOverlayConfigFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex,
                    "const/4 v$targetRegister, 0x0"
                )
            }
        }

    }
}
