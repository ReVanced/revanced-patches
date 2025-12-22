package app.revanced.patches.com.sbs.ondemand.tv

import app.revanced.patcher.fingerprint

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
    custom { method, classDef ->
        method.name == "requestAdStream\$player_googleStoreTvRelease" &&
        classDef.type == "Lcom/sbs/ondemand/player/viewmodels/AdsController;"
    }
}

