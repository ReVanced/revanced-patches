package app.revanced.patches.instagram.misc.links

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.returnType

internal const val TARGET_STRING = "Tracking.ARG_CLICK_SOURCE"

internal val inAppBrowserFunctionMethodMatch = firstMethodComposite {
    instructions("TrackingInfo.ARG_MODULE_NAME"(), TARGET_STRING())
    returnType("Z")
}
