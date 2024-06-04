package app.revanced.patches.youtube.misc.zoomhaptics.fingerprints
import app.revanced.patcher.fingerprint.methodFingerprint

internal val zoomHapticsFingerprint = methodFingerprint {
    strings("Failed to haptics vibrate for video zoom")
}
