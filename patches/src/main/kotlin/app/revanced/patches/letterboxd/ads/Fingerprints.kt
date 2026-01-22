package app.revanced.patches.letterboxd.ads

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal const val admobHelperClassName = "Lcom/letterboxd/letterboxd/helpers/AdmobHelper;"

internal val BytecodePatchContext.admobHelperSetShowAdsMethod by gettingFirstMutableMethodDeclaratively {
    name("setShowAds")
    definingClass(admobHelperClassName)
}

internal val BytecodePatchContext.admobHelperShouldShowAdsMethod by gettingFirstMutableMethodDeclaratively {
    name("shouldShowAds")
    definingClass(admobHelperClassName)
}

internal val BytecodePatchContext.filmFragmentShowAdsMethod by gettingFirstMutableMethodDeclaratively {
    name("showAds")
    definingClass("/FilmFragment;"::endsWith)

}

internal val BytecodePatchContext.memberExtensionShowAdsMethod by gettingFirstMutableMethodDeclaratively {
    name("showAds")
    definingClass("/AMemberExtensionKt;"::endsWith)
}
