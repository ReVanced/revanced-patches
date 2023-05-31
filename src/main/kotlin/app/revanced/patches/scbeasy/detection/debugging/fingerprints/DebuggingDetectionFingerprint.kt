package app.revanced.patches.scbeasy.detection.debugging.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object DebuggingDetectionFingerprint : MethodFingerprint(
    returnType = "Z",
    strings = listOf("adb_enabled")
)
