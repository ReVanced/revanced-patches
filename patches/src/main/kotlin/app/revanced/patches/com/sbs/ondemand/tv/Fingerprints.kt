package app.revanced.patches.com.sbs.ondemand.tv

import app.revanced.patcher.fingerprint

// Advertisement-related fingerprints
internal val shouldShowAdvertisingTVFingerprint = fingerprint {
    returns("Z")
    custom { method, classDef ->
        method.name == "getShouldShowAdvertisingTV" &&
        classDef.type == "Lcom/sbs/ondemand/common/InMemoryStorage;"
    }
}

internal val shouldShowPauseAdFingerprint = fingerprint {
    returns("Z")
    custom { method, classDef ->
        method.name == "shouldShowPauseAd" &&
        classDef.type == "Lcom/sbs/ondemand/player/viewmodels/PauseAdController;"
    }
}

internal val requestAdStreamFingerprint = fingerprint {
    returns("V")
    // Matching the method with .startsWith(), because the AntiSplit-M APK (using APKEditor)
    // is adding variant suffix's to the method to be patched (eg. $player_googleStoreTvRelease)
    // and it should work on any variant.
    custom { method, classDef ->
        method.name.startsWith("requestAdStream") &&
        classDef.type == "Lcom/sbs/ondemand/player/viewmodels/AdsController;"
    }
}


