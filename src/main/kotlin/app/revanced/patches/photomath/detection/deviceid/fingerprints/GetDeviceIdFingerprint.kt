package app.revanced.patches.photomath.detection.deviceid.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getDeviceIdFingerprint = methodFingerprint {
    returns("Ljava/lang/String;")
    strings("androidId", "android_id")
    parameters()
}