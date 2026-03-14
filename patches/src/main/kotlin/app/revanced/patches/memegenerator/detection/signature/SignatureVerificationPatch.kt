package app.revanced.patches.memegenerator.detection.signature

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

val signatureVerificationPatch = bytecodePatch(
    description = "Disables detection of incorrect signature.",
) {

    apply {
        verifySignatureMethod.returnEarly(true)
    }
}
