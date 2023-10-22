package app.revanced.patches.scbeasy.detection.debugging.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object DebuggingDetectionFingerprint : MethodFingerprint(
    returnType = "Z",
    strings = listOf("adb_enabled")
)
