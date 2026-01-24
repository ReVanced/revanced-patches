package app.revanced.patches.photomath.detection.deviceid

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.util.returnEarly
import kotlin.random.Random

@Suppress("unused", "ObjectPropertyName")
val `Spoof device ID` by creatingBytecodePatch(
    description = "Spoofs device ID to mitigate manual bans by developers.",
) {
    dependsOn(signatureDetectionPatch)

    compatibleWith("com.microblink.photomath")

    apply {
        getDeviceIdMethod.returnEarly(Random.nextLong().toString(16))
    }
}
