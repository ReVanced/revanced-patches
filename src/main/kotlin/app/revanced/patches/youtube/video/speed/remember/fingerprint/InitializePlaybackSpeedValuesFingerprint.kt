package app.revanced.patches.youtube.video.speed.remember.fingerprint

import app.revanced.patcher.fingerprint.methodFingerprint

internal val initializePlaybackSpeedValuesFingerprint = methodFingerprint {
    parameters("[L", "I")
    strings("menu_item_playback_speed")
}
