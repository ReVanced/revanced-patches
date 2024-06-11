package app.revanced.patches.youtube.misc.zoomhaptics

import app.revanced.patcher.fingerprint.methodFingerprint

internal val zoomHapticsFingerprint = methodFingerprint {
    strings("Failed to haptics vibrate for video zoom")
}
