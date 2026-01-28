package app.revanced.patches.photomath.misc.unlock.plus

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.patches.photomath.misc.unlock.bookpoint.enableBookpointPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val unlockPlusPatch = bytecodePatch("Unlock plus") {
    dependsOn(signatureDetectionPatch, enableBookpointPatch)

    compatibleWith("com.microblink.photomath")

    apply {
        isPlusUnlockedMethod.returnEarly(true)
    }
}
