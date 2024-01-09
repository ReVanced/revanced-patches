package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ShowOldPlaybackSpeedMenuFingerprint : MethodFingerprint(
    strings = listOf("PLAYBACK_RATE_MENU_BOTTOM_SHEET_FRAGMENT")
)
