package app.revanced.patches.youtube.misc.zoomhaptics

import app.revanced.patcher.fingerprint

internal val zoomHapticsFingerprint = fingerprint {
    strings("Failed to haptics vibrate for video zoom")
}
