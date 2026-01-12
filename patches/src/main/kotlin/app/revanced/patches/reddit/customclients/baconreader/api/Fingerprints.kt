package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getAuthorizationUrlMethod by gettingFirstMutableMethodDeclaratively(
    "client_id=zACVn0dSFGdWqQ"
)

internal val BytecodePatchContext.getClientIdMethod by gettingFirstMutableMethodDeclaratively("client_id=zACVn0dSFGdWqQ") {
    name("getAuthorizeUrl")
    definingClass { endsWith("RedditOAuth;") }
}

internal val BytecodePatchContext.requestTokenMethod by gettingFirstMutableMethodDeclaratively(
    "zACVn0dSFGdWqQ",
    "kDm2tYpu9DqyWFFyPlNcXGEni4k"
)
