package app.revanced.patches.instagram.misc.links

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal const val TARGET_STRING = "Tracking.ARG_CLICK_SOURCE"

internal val BytecodePatchContext.inAppBrowserFunctionMethod by gettingFirstMutableMethodDeclaratively(
    "TrackingInfo.ARG_MODULE_NAME",
    TARGET_STRING
) {
    returnType("Z")
}
