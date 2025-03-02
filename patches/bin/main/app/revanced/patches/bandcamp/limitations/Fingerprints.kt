package app.revanced.patches.bandcamp.limitations

import app.revanced.patcher.fingerprint

internal val handlePlaybackLimitsFingerprint = fingerprint {
    strings("play limits processing track", "found play_count")
}
