package app.revanced.patches.bandcamp.limitations

import app.revanced.patcher.fingerprint

internal val handlePlaybackLimitsFingerprint by fingerprint {
    strings("track_id", "play_count")
}
