package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ShowOldVideoSpeedMenuFingerprint : MethodFingerprint(
    strings = listOf("PLAYBACK_RATE_MENU_BOTTOM_SHEET_FRAGMENT")
)
