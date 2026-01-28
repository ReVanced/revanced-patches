package app.revanced.patches.youtube.ad.video

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.loadVideoAdsMethod by gettingFirstMethod(
    "TriggerBundle doesn't have the required metadata specified by the trigger ",
    "Ping migration no associated ping bindings for activated trigger: ",
)
