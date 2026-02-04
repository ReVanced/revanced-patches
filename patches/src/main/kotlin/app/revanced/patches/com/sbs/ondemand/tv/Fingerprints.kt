package app.revanced.patches.com.sbs.ondemand.tv

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

val BytecodePatchContext.shouldShowAdvertisingTVMethod by gettingFirstMethodDeclaratively {
    name("getShouldShowAdvertisingTV")
    definingClass("Lcom/sbs/ondemand/common/InMemoryStorage;")
    returnType("Z")
}

internal val BytecodePatchContext.shouldShowPauseAdMethod by gettingFirstMethodDeclaratively {
    name("shouldShowPauseAd")
    definingClass("Lcom/sbs/ondemand/player/viewmodels/PauseAdController;")
    returnType("Z")
}

internal val BytecodePatchContext.requestAdStreamMethod by gettingFirstMethodDeclaratively {
    name("requestAdStream\$player_googleStoreTvRelease")
    definingClass("Lcom/sbs/ondemand/player/viewmodels/AdsController;")
    returnType("V")
}
