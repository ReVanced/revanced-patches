package app.revanced.patches.bandcamp.removeplaylimits.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object playLimitsFingerprint : MethodFingerprint(
    strings = listOf("play limits processing track", "found play_count"),
)
