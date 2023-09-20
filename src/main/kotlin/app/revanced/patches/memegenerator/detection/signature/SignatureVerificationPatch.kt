package app.revanced.patches.memegenerator.detection.signature

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.memegenerator.detection.signature.fingerprints.VerifySignatureFingerprint

@Patch(description = "Disables detection of incorrect signature.")
object SignatureVerificationPatch : BytecodePatch(
    setOf(VerifySignatureFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        VerifySignatureFingerprint.result?.apply {
            mutableMethod.replaceInstructions(
                0,
                """
                    const/4 p0, 0x1
                    return  p0
                """
            )
        } ?: throw VerifySignatureFingerprint.exception
    }
}