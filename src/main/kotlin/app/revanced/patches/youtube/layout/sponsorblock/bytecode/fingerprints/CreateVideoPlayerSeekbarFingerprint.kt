package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CreateVideoPlayerSeekbarFingerprint : MethodFingerprint(
    "V",
    strings = listOf("timed_markers_width")
)