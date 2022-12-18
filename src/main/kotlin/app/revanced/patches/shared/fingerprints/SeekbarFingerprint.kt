package app.revanced.patches.shared.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SeekbarFingerprint : MethodFingerprint(
    "V",
    strings = listOf("timed_markers_width")
)