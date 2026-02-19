package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.buildUserAgentMethod by gettingFirstMethodDeclaratively(
    "%s:%s:%s (by /u/%s)",
)

internal val BytecodePatchContext.getClientIdMethod by gettingFirstMethodDeclaratively {
    name("getClientId")
    definingClass { endsWith("Credentials;") }
}
