package app.revanced.patches.youtube.video.information.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CreateVideoPlayerSeekbarFingerprint : MethodFingerprint(
    "V",
    strings = listOf("timed_markers_width")
)