package app.revanced.patches.youtube.misc.video.speed.remember.fingerprint

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ChangePlaybackSpeedFragmentStateFingerprint : MethodFingerprint(
    "V",
    strings = listOf("PLAYBACK_RATE_MENU_BOTTOM_SHEET_FRAGMENT")
)