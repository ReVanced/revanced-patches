package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getClientIdMethod by gettingFirstMutableMethodDeclaratively {
    name("getClientId")
    definingClass("Credentials;"::endsWith)
}
