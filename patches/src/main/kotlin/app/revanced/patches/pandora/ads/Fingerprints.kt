package app.revanced.patches.pandora.ads

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getIsAdSupportedMethod by gettingFirstMutableMethodDeclaratively {
    name("getIsAdSupported")
    definingClass("UserData;")
}

internal val BytecodePatchContext.requestAudioAdMethod by gettingFirstMutableMethodDeclaratively {
    name("requestAudioAdFromAdSDK")
    definingClass { endsWith("ContentServiceOpsImpl;") }
}
