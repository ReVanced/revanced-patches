package app.revanced.patches.photomath.detection.signature

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.photomath.detection.signature.fingerprints.CheckSignatureFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(description = "Disables detection of incorrect signature.")
object SignatureDetectionPatch : BytecodePatch(
    setOf(CheckSignatureFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        CheckSignatureFingerprint.result?.apply {
            val signatureCheckInstruction = mutableMethod.getInstruction(scanResult.patternScanResult!!.endIndex)
            val checkRegister = (signatureCheckInstruction as OneRegisterInstruction).registerA

            mutableMethod.replaceInstruction(signatureCheckInstruction.location.index, "const/4 v$checkRegister, 0x1")
        } ?: throw CheckSignatureFingerprint.exception
    }
}
