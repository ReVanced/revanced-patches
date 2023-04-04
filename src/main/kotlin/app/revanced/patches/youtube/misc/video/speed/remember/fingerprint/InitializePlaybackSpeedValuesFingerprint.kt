package app.revanced.patches.youtube.misc.video.speed.current.fingerprint

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object InitializePlaybackSpeedValuesFingerprint : MethodFingerprint(
    parameters = listOf("[L", "I"),
    strings = listOf("menu_item_playback_speed")
)