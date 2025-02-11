package app.revanced.patches.twitch.ad.video

import app.revanced.patcher.fingerprint

internal val checkAdEligibilityLambdaFingerprint by fingerprint {
    returns("Lio/reactivex/Single;")
    parameters("L")
    custom { method, _ ->
        method.definingClass.endsWith("/AdEligibilityFetcher;") &&
            method.name == "shouldRequestAd"
    }
}

internal val contentConfigShowAdsFingerprint by fingerprint {
    returns("Z")
    parameters()
    custom { method, _ ->
        method.definingClass.endsWith("/ContentConfigData;") && method.name == "getShowAds"
    }
}

internal val getReadyToShowAdFingerprint by fingerprint {
    returns("Ltv/twitch/android/core/mvp/presenter/StateAndAction;")
    parameters("L", "L")
    custom { method, _ ->
        method.definingClass.endsWith("/StreamDisplayAdsPresenter;") && method.name == "getReadyToShowAdOrAbort"
    }
}
