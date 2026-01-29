package app.revanced.patches.instagram.misc.links

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal const val TARGET_STRING = "Tracking.ARG_CLICK_SOURCE"

internal val BytecodePatchContext.inAppBrowserFunctionMethodMatch by composingFirstMethod("TrackingInfo.ARG_MODULE_NAME") {
    instructions(TARGET_STRING())
    returnType("Z")
}
