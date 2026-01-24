package app.revanced.patches.photomath.misc.annoyances

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch

@Suppress("unused", "ObjectPropertyName")
val `Hide update popup` by creatingBytecodePatch(
    description = "Prevents the update popup from showing up.",
) {
    dependsOn(signatureDetectionPatch)

    compatibleWith("com.microblink.photomath")

    apply {
        hideUpdatePopupMethod.addInstructions(
            2, // Insert after the null check.
            "return-void",
        )
    }
}
