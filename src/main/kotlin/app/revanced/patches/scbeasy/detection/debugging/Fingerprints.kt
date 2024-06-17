package app.revanced.patches.scbeasy.detection.debugging

import app.revanced.patcher.fingerprint

internal val debuggingDetectionFingerprint = fingerprint {
    returns("Z")
    strings("adb_enabled")
}