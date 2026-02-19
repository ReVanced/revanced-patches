package app.revanced.patches.letterboxd.ads

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal const val admobHelperClassName = "Lcom/letterboxd/letterboxd/helpers/AdmobHelper;"

internal val BytecodePatchContext.admobHelperSetShowAdsMethod by gettingFirstMethodDeclaratively {
    name("setShowAds")
    definingClass(admobHelperClassName)
}

internal val BytecodePatchContext.admobHelperShouldShowAdsMethod by gettingFirstMethodDeclaratively {
    name("shouldShowAds")
    definingClass(admobHelperClassName)
}

internal val BytecodePatchContext.filmFragmentShowAdsMethod by gettingFirstMethodDeclaratively {
    name("showAds")
    definingClass { endsWith("/FilmFragment;") }
}

internal val BytecodePatchContext.memberExtensionShowAdsMethod by gettingFirstMethodDeclaratively {
    name("showAds")
    definingClass { endsWith("/AMemberExtensionKt;") }
}
