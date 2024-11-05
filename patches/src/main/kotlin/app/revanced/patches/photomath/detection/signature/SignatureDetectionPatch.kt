package app.revanced.patches.photomath.detection.signature

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

val signatureDetectionPatch = bytecodePatch(
    description = "Disables detection of incorrect signature.",
) {

    execute {
        val replacementIndex = checkSignatureFingerprint.patternMatch!!.endIndex
        val checkRegister =
            checkSignatureFingerprint.method.getInstruction<OneRegisterInstruction>(replacementIndex).registerA
        checkSignatureFingerprint.method.replaceInstruction(replacementIndex, "const/4 v$checkRegister, 0x1")
    }
}
