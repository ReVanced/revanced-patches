package app.revanced.patches.letterboxd.ads

import app.revanced.patcher.fingerprint

internal const val admobHelperClassName = "Lcom/letterboxd/letterboxd/helpers/AdmobHelper;"

val admobHelperSetShowAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "setShowAds" && classDef.type == admobHelperClassName
    }
}

val admobHelperShouldShowAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "shouldShowAds" && classDef.type == admobHelperClassName
    }
}

val filmFragmentShowAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "showAds" && classDef.type.endsWith("/FilmFragment;")
    }
}

val memberExtensionShowAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "showAds" && classDef.type.endsWith("/AMemberExtensionKt;")
    }
}