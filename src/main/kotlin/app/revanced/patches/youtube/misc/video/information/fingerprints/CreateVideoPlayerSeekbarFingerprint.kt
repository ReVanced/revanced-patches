package app.revanced.patches.youtube.misc.video.information.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CreateVideoPlayerSeekbarFingerprint : MethodFingerprint(
    "V",
    strings = listOf("timed_markers_width")
)