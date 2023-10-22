package app.revanced.patches.youtube.video.speed.remember.fingerprint

import app.revanced.patcher.fingerprint.MethodFingerprint

object InitializePlaybackSpeedValuesFingerprint : MethodFingerprint(
    parameters = listOf("[L", "I"),
    strings = listOf("menu_item_playback_speed")
)