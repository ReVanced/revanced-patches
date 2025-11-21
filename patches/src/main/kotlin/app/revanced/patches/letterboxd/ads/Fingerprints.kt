package app.revanced.patches.letterboxd.ads

import app.revanced.patcher.fingerprint

internal const val admobHelperClassName = "Lcom/letterboxd/letterboxd/helpers/AdmobHelper;"

internal val admobHelperSetShowAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "setShowAds" && classDef.type == admobHelperClassName
    }
}

internal val admobHelperShouldShowAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "shouldShowAds" && classDef.type == admobHelperClassName
    }
}

internal val filmFragmentShowAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "showAds" && classDef.type.endsWith("/FilmFragment;")
    }
}

internal val memberExtensionShowAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "showAds" && classDef.type.endsWith("/AMemberExtensionKt;")
    }
}