package app.revanced.patches.twitch.ad.video

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkAdEligibilityLambdaFingerprint = methodFingerprint {
    returns("Lio/reactivex/Single;")
    parameters("L")
    custom { method, _ ->
        method.definingClass.endsWith("/AdEligibilityFetcher;") &&
            method.name == "shouldRequestAd"
    }
}

internal val contentConfigShowAdsFingerprint = methodFingerprint {
    returns("Z")
    parameters()
    custom { method, _ ->
        method.definingClass.endsWith("/ContentConfigData;") && method.name == "getShowAds"
    }
}

internal val getReadyToShowAdFingerprint = methodFingerprint {
    returns("Ltv/twitch/android/core/mvp/presenter/StateAndAction;")
    parameters("L", "L")
    custom { method, _ ->
        method.definingClass.endsWith("/StreamDisplayAdsPresenter;") && method.name == "getReadyToShowAdOrAbort"
    }
}
