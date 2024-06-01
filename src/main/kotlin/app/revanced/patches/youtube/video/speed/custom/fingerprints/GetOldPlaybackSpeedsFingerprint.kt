package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getOldPlaybackSpeedsFingerprint = methodFingerprint {
    parameters("[L", "I")
    strings("menu_item_playback_speed")
}
