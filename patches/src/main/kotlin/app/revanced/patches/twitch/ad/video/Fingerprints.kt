package app.revanced.patches.twitch.ad.video

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.checkAdEligibilityLambdaMethod by gettingFirstMethodDeclaratively {
    name("shouldRequestAd")
    definingClass("/AdEligibilityFetcher;")
    returnType("Lio/reactivex/Single;")
    parameterTypes("L")
}

internal val BytecodePatchContext.contentConfigShowAdsMethod by gettingFirstMethodDeclarativelyOrNull {
    name("getShowAds")
    definingClass("/ContentConfigData;")
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.getReadyToShowAdMethod by gettingFirstMethodDeclaratively {
    name("getReadyToShowAdOrAbort")
    definingClass("/StreamDisplayAdsPresenter;")
    returnType("Ltv/twitch/android/core/mvp/presenter/StateAndAction;")
    parameterTypes("L", "L")
}
