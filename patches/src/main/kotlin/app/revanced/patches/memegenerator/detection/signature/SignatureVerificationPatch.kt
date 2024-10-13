package app.revanced.patches.memegenerator.detection.signature

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val signatureVerificationPatch = bytecodePatch(
    description = "Disables detection of incorrect signature.",
) {
    val verifySignatureMatch by verifySignatureFingerprint()

    execute {
        verifySignatureMatch.mutableMethod.replaceInstructions(
            0,
            """
                const/4 p0, 0x1
                return  p0
            """,
        )
    }
}
