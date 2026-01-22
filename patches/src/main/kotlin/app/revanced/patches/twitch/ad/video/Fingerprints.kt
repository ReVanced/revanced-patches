package app.revanced.patches.twitch.ad.video

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.checkAdEligibilityLambdaMethod by gettingFirstMutableMethodDeclaratively {
    name("shouldRequestAd")
    definingClass("/AdEligibilityFetcher;"::endsWith)
    returnType("Lio/reactivex/Single;")
    parameterTypes("L")
}

internal val BytecodePatchContext.contentConfigShowAdsMethod by gettingFirstMutableMethodDeclaratively {
    name("getShowAds")
    definingClass("/ContentConfigData;"::endsWith)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.getReadyToShowAdMethod by gettingFirstMutableMethodDeclaratively {
    name("getReadyToShowAdOrAbort")
    definingClass("/StreamDisplayAdsPresenter;"::endsWith)
    returnType("Ltv/twitch/android/core/mvp/presenter/StateAndAction;")
    parameterTypes("L", "L")
}
