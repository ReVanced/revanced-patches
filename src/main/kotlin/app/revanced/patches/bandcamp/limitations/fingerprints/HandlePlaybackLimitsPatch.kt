package app.revanced.patches.bandcamp.limitations.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object HandlePlaybackLimitsPatch : MethodFingerprint(
    strings = listOf("play limits processing track", "found play_count"),
)
