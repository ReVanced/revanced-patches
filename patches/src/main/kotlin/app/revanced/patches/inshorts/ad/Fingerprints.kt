package app.revanced.patches.inshorts.ad

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.inshortsAdsMethod by gettingFirstMethodDeclaratively(
    "GoogleAdLoader", "exception in requestAd"
) {
    returnType("V")
}