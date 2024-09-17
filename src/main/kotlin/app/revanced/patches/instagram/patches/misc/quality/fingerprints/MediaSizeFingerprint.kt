package app.revanced.patches.instagram.patches.maxQuality.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MediaSizeFingerprint : MethodFingerprint(
    strings = listOf("_8.jpg", "_6.jpg"),
)
