package app.revanced.patches.youtube.ad.video

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.loadVideoAdsMethod by gettingFirstMethodDeclaratively(
    "TriggerBundle doesn't have the required metadata specified by the trigger ",
    "Ping migration no associated ping bindings for activated trigger: ",
)

internal val BytecodePatchContext.playerBytesAdLayoutMethod by gettingFirstMethodDeclaratively(
    "Bootstrapped layout construction resulted in non PlayerBytesLayout. PlayerAds count: "
) {
    returnType("V")
    parameterTypes("L")
}
