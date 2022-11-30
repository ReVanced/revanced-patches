package app.revanced.patches.twitch.ad.video.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object AdsManagerFingerprint : MethodFingerprint(
    customFingerprint = { method ->
        method.definingClass.endsWith("AdsManagerImpl;") && method.name == "playAds"
    }
)