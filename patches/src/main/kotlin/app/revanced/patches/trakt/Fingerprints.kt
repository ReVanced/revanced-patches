package app.revanced.patches.trakt

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.isVIPEPMethod by gettingFirstMutableMethodDeclaratively {
    name("isVIPEP")
    definingClass { endsWith("RemoteUser;") }
}

internal val BytecodePatchContext.isVIPMethod by gettingFirstMutableMethodDeclaratively {
    name("isVIP")
    definingClass { endsWith("RemoteUser;") }
}
