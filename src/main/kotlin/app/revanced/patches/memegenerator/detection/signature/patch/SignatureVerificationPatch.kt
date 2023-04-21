package app.revanced.patches.memegenerator.detection.signature.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.memegenerator.detection.signature.fingerprint.VerifySignatureFingerprint

@Description("Disables detection of incorrect signature.")
@Version("0.0.1")
class SignatureVerificationPatch : BytecodePatch(
    listOf(VerifySignatureFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        VerifySignatureFingerprint.result?.apply {
            mutableMethod.replaceInstructions(
                0,
                """
                    const/4 p0, 0x1
                    return  p0
                """
            )
        } ?: throw VerifySignatureFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}