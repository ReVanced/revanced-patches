package app.revanced.patches.photomath.misc.annoyances

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.photomath.detection.signature.`Signature detection`


@Suppress("unused")
val `Hide update popup` by creatingBytecodePatch(
    description = "Prevents the update popup from showing up.",
) {
    dependsOn(`Signature detection`)

    compatibleWith("com.microblink.photomath")

    apply {
        hideUpdatePopupMethod.addInstructions(
            2, // Insert after the null check.
            "return-void",
        )
    }
}
