package app.revanced.patches.scbeasy.detection.debugging

import app.revanced.patcher.fingerprint.methodFingerprint

internal val debuggingDetectionFingerprint = methodFingerprint {
    returns("Z")
    strings("adb_enabled")
}