package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.fingerprint

internal val initializePlaybackSpeedValuesFingerprint = fingerprint {
    parameters("[L", "I")
    strings("menu_item_playback_speed")
}
