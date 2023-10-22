package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object ShowOldPlaybackSpeedMenuIntegrationsFingerprint : MethodFingerprint(
    customFingerprint = { method, _ -> method.name == "showOldPlaybackSpeedMenu" }
)
