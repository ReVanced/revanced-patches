package app.revanced.patches.inshorts.ad

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.inshortsAdsMethod by gettingFirstMutableMethodDeclaratively(
    "GoogleAdLoader", "exception in requestAd"
) {
    returnType("V")
}