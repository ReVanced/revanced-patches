package app.revanced.patches.twitch.ad.audio

import app.revanced.patcher.fingerprint

internal val audioAdsPresenterPlayFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("AudioAdsPlayerPresenter;") && method.name == "playAd"
    }
}
