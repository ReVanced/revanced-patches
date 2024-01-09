package app.revanced.patches.twitch.ad.audio.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object AudioAdsPresenterPlayFingerprint : MethodFingerprint(
    customFingerprint = { method, _ ->
        method.definingClass.endsWith("AudioAdsPlayerPresenter;") && method.name == "playAd"
    }
)