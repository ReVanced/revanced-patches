package app.revanced.patches.protonvpn.delay

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.longDelayMethod by gettingFirstMethodDeclaratively {
    definingClass("AppConfigResponse;")
    name("getChangeServerLongDelayInSeconds")
}

internal val BytecodePatchContext.shortDelayMethod by gettingFirstMethodDeclaratively {
    definingClass("AppConfigResponse;")
    name("getChangeServerShortDelayInSeconds")
}
