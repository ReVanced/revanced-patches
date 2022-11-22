package app.revanced.patches.twitch.ad.audio.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object AudioAdsPresenterPlayFingerprint : MethodFingerprint(
    customFingerprint = { method ->
        method.definingClass.endsWith("AudioAdsPlayerPresenter;") && method.name == "playAd"
    }
)