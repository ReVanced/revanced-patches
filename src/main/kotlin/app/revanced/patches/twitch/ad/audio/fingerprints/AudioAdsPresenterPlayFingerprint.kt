package app.revanced.patches.twitch.ad.audio.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val audioAdsPresenterPlayFingerprint = methodFingerprint {
    custom { method, classDef ->
        classDef.endsWith("AudioAdsPlayerPresenter;") && method.name == "playAd"
    }
}
