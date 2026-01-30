package app.revanced.patches.kleinanzeigen.ads

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getLibertyInitMethod by gettingFirstMutableMethodDeclaratively {
    name("init")
    definingClass { endsWith("/Liberty;") }
}
