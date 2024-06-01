package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val showOldPlaybackSpeedMenuIntegrationsFingerprint = methodFingerprint {
    custom { method, _ -> method.name == "showOldPlaybackSpeedMenu" }
}
