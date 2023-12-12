package app.revanced.patches.photomath.detection.deviceid.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object GetDeviceIdFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    strings = listOf("androidId", "android_id"),
    parameters = listOf()
)