package app.revanced.patches.photomath.misc.unlock.plus

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.patches.photomath.misc.unlock.bookpoint.enableBookpointPatch

@Suppress("unused", "ObjectPropertyName")
val `Unlock plus` by creatingBytecodePatch {
    dependsOn(signatureDetectionPatch, enableBookpointPatch)

    compatibleWith("com.microblink.photomath")

    apply {
        isPlusUnlockedMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
