package app.revanced.patches.protonvpn.delay

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.longDelayMethod by gettingFirstMutableMethodDeclaratively {
    name("getChangeServerLongDelayInSeconds")
}

internal val BytecodePatchContext.shortDelayMethod by gettingFirstMutableMethodDeclaratively {
    name("getChangeServerShortDelayInSeconds")
}
