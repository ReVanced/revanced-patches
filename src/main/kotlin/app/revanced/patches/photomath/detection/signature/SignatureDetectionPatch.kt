package app.revanced.patches.photomath.detection.signature

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val signatureDetectionPatch = bytecodePatch(
    description = "Disables detection of incorrect signature.",
) {
    val checkSignatureFingerprintResult by checkSignatureFingerprint()

    execute {
        val signatureCheckInstruction = checkSignatureFingerprintResult.mutableMethod.getInstruction(
            checkSignatureFingerprintResult.scanResult.patternScanResult!!.endIndex,
        )
        val checkRegister = (signatureCheckInstruction as OneRegisterInstruction).registerA

        checkSignatureFingerprintResult.mutableMethod.replaceInstruction(
            signatureCheckInstruction.location.index,
            "const/4 v$checkRegister, 0x1",
        )
    }
}
