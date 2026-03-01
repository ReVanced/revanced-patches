package app.revanced.patches.crunchyroll.ads

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.videoUrlReadyToStringMethodMatch by composingFirstMethod {
    instructions("VideoUrlReady(url="(), ", enableAds="())
}
