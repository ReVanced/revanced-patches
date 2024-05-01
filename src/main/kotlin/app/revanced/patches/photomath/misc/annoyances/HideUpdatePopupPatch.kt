package app.revanced.patches.photomath.misc.annoyances

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.patches.photomath.misc.annoyances.fingerprints.hideUpdatePopupFingerprint

@Suppress("unused")
val hideUpdatePopupPatch = bytecodePatch(
    name = "Hide update popup",
    description = "Prevents the update popup from showing up.",
) {
    compatibleWith("com.microblink.photomath"("8.32.0"))
    dependsOn(signatureDetectionPatch)

    val hideUpdatePopupResult by hideUpdatePopupFingerprint

    hideUpdatePopupResult.mutableMethod.addInstructions(
        2, // Insert after the null check.
        "return-void"
    )
}