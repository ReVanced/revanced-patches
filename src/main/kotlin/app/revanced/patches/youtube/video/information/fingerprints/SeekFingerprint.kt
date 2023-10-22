package app.revanced.patches.youtube.video.information.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

object SeekFingerprint : MethodFingerprint(
    strings = listOf("Attempting to seek during an ad")
)