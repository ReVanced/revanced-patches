package app.revanced.patches.youtube.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val seekbarFingerprint = methodFingerprint {
    returns("V")
    strings("timed_markers_width")
}
