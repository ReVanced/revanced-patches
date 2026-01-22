package app.revanced.patches.com.sbs.ondemand.tv

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.shouldShowAdvertisingTVMethod by gettingFirstMutableMethodDeclaratively {
    name("getShouldShowAdvertisingTV")
    definingClass("Lcom/sbs/ondemand/common/InMemoryStorage;")
    returnType("Z")
}

internal val BytecodePatchContext.shouldShowPauseAdMethod by gettingFirstMutableMethodDeclaratively {
    name("shouldShowPauseAd")
    definingClass("Lcom/sbs/ondemand/player/viewmodels/PauseAdController;")
    returnType("Z")

}

internal val BytecodePatchContext.requestAdStreamMethod by gettingFirstMutableMethodDeclaratively {
    name("requestAdStream\$player_googleStoreTvRelease")
    definingClass("Lcom/sbs/ondemand/player/viewmodels/AdsController;")
    returnType("V")
}