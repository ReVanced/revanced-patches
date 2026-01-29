package app.revanced.patches.photomath.detection.signature

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val signatureDetectionPatch = bytecodePatch(
    description = "Disables detection of incorrect signature.",
) {
    apply {
        val replacementIndex = checkSignatureMethodMatch[-1]

        val checkRegister = checkSignatureMethodMatch.method.getInstruction<OneRegisterInstruction>(replacementIndex)
            .registerA
        checkSignatureMethodMatch.method.replaceInstruction(replacementIndex, "const/4 v$checkRegister, 0x1")
    }
}
