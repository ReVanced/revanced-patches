package app.revanced.patches.scbeasy.detectiong.debugging.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object DebuggingDetectionFingerprint : MethodFingerprint(
    returnType = "Z",
    strings = listOf("adb_enabled")
)
