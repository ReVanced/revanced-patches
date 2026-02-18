package app.revanced.patches.kleinanzeigen.ads

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getLibertyInitMethod by gettingFirstMethodDeclaratively {
    name("init")
    definingClass("/Liberty;")
}
