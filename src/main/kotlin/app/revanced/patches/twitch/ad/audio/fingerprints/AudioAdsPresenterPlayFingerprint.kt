package app.revanced.patches.twitch.ad.audio.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val audioAdsPresenterPlayFingerprint = methodFingerprint {
    custom { method, _ ->
        method.definingClass.endsWith("AudioAdsPlayerPresenter;") && method.name == "playAd"
    }
}
