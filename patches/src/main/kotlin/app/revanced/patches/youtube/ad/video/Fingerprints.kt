package app.revanced.patches.youtube.ad.video

internal val BytecodePatchContext.loadVideoAdsMethod by gettingFirstMethodDeclaratively {
    strings(
        "TriggerBundle doesn't have the required metadata specified by the trigger ",
        "Ping migration no associated ping bindings for activated trigger: ",
    )
}
