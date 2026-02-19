package app.revanced.patches.trakt

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.isVIPEPMethod by gettingFirstMethodDeclaratively {
    name("isVIPEP")
    definingClass { endsWith("RemoteUser;") }
}

internal val BytecodePatchContext.isVIPMethod by gettingFirstMethodDeclaratively {
    name("isVIP")
    definingClass { endsWith("RemoteUser;") }
}
