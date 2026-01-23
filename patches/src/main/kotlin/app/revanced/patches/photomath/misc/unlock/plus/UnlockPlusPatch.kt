package app.revanced.patches.photomath.misc.unlock.plus

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.patches.photomath.misc.unlock.bookpoint.enableBookpointPatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Unlock plus` by creatingBytecodePatch {
    dependsOn(signatureDetectionPatch, enableBookpointPatch)

    compatibleWith("com.microblink.photomath")

    apply {
        isPlusUnlockedMethod.returnEarly(true)
    }
}
