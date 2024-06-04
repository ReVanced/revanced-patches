package app.revanced.patches.photomath.misc.annoyances

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.patches.photomath.misc.annoyances.fingerprints.hideUpdatePopupFingerprint

@Suppress("unused")
val hideUpdatePopupPatch = bytecodePatch(
    name = "Hide update popup",
    description = "Prevents the update popup from showing up.",
    dependencies = [SignatureDetectionPatch::class],
    compatiblePackages = [CompatiblePackage("com.microblink.photomath", ["8.37.0"])]
)
@Suppress("unused")
object HideUpdatePopupPatch : BytecodePatch(
    setOf(HideUpdatePopupFingerprint)
) {
    dependsOn(signatureDetectionPatch)

    compatibleWith("com.microblink.photomath"("8.32.0"))

    val hideUpdatePopupResult by hideUpdatePopupFingerprint

    hideUpdatePopupResult.mutableMethod.addInstructions(
        2, // Insert after the null check.
        "return-void"
    )
}