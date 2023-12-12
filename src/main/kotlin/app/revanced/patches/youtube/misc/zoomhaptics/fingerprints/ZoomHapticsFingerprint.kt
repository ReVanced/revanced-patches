package app.revanced.patches.youtube.misc.zoomhaptics.fingerprints
import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ZoomHapticsFingerprint : MethodFingerprint(
    strings = listOf(
        "Failed to haptics vibrate for video zoom"
    )
)