package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getClientIdMethod by gettingFirstMethodDeclaratively {
    name("getClientId")
    definingClass("Credentials;")
}
