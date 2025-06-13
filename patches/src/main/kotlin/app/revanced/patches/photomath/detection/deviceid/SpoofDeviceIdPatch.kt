package app.revanced.patches.photomath.detection.deviceid

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.photomath.detection.signature.signatureDetectionPatch
import app.revanced.util.returnEarly
import kotlin.random.Random

@Suppress("unused")
val getDeviceIdPatch = bytecodePatch(
    name = "Spoof device ID",
    description = "Spoofs device ID to mitigate manual bans by developers.",
) {
    dependsOn(signatureDetectionPatch)

    compatibleWith("com.microblink.photomath")

    execute {
        getDeviceIdFingerprint.method.returnEarly(Random.nextLong().toString(16))
    }
}
