package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ShowOldVideoSpeedMenuIntegrationsFingerprint : MethodFingerprint(
    customFingerprint = { method, _ -> method.name == "showOldVideoSpeedMenu" }
)
