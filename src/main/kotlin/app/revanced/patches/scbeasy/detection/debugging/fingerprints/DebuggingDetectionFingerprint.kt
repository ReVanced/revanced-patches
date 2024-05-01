package app.revanced.patches.scbeasy.detection.debugging.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal var debuggingDetectionFingerprint = methodFingerprint {
    returns("Z")
    strings("adb_enabled")
}
