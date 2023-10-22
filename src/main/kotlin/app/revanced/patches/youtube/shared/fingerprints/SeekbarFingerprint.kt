package app.revanced.patches.youtube.shared.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

object SeekbarFingerprint : MethodFingerprint(
    "V",
    strings = listOf("timed_markers_width")
)