package app.revanced.patches.youtube.misc.video.speed.remember.fingerprint

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object InitializePlaybackSpeedValuesFingerprint : MethodFingerprint(
    parameters = listOf("[L", "I")
)