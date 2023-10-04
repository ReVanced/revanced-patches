package app.revanced.patches.youtube.shared.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SeekbarFingerprint : MethodFingerprint(
    "V",
    strings = listOf("timed_markers_width")
)