package app.revanced.patches.crunchyroll.ads

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.videoUrlReadyToStringMethod by gettingFirstMutableMethodDeclaratively(
    "VideoUrlReady(url=", ", enableAds="
)
