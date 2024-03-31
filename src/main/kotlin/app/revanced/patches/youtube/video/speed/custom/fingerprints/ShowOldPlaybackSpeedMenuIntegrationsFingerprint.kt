package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ShowOldPlaybackSpeedMenuIntegrationsFingerprint : MethodFingerprint(
    customFingerprint = { method, _ -> method.name == "showOldPlaybackSpeedMenu" }
)
