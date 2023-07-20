package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetOldPlaybackSpeedsFingerprint : MethodFingerprint(
    parameters = listOf("[L", "I"),
    strings = listOf("menu_item_playback_speed")
)
