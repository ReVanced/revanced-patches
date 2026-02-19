package app.revanced.patches.pandora.misc

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getSkipLimitBehaviorMethod by gettingFirstMethodDeclaratively {
    name("getSkipLimitBehavior")
    definingClass { endsWith("UserData;") }
}
