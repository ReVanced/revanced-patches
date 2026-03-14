package app.revanced.patches.pandora.ads

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getIsAdSupportedMethod by gettingFirstMethodDeclaratively {
    name("getIsAdSupported")
    definingClass("UserData;")
}

internal val BytecodePatchContext.requestAudioAdMethod by gettingFirstMethodDeclaratively {
    name("requestAudioAdFromAdSDK")
    definingClass("ContentServiceOpsImpl;")
}
