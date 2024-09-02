package app.revanced.patches.instagram.patches.maxQuality.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object StoryMediaBitrateFingerprint : MethodFingerprint(
    strings = listOf("color-format", "bitrate", "frame-rate", "i-frame-interval", "profile", "level"),
    returnType = "Landroid/media/MediaFormat;",
)
