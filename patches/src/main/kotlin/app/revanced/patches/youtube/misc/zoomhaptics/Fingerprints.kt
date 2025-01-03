package app.revanced.patches.youtube.misc.zoomhaptics

import app.revanced.patcher.fingerprint

internal val zoomHapticsFingerprint by fingerprint {
    strings("Failed to haptics vibrate for video zoom")
}
