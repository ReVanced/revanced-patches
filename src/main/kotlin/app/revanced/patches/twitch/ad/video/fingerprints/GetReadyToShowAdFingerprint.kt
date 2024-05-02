package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getReadyToShowAdFingerprint = methodFingerprint {
    returns("Ltv/twitch/android/core/mvp/presenter/StateAndAction;")
    parameters("L", "L")
    custom { method, _ ->
        method.definingClass.endsWith("/StreamDisplayAdsPresenter;") && method.name == "getReadyToShowAdOrAbort"
    }
}
