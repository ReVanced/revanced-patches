package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethod
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.linkHelperOpenLinkMethod by gettingFirstMutableMethod("Link title: ")

internal val BytecodePatchContext.setAuthorizationHeaderMethod by gettingFirstMutableMethodDeclaratively(
    "Authorization",
    "bearer ",
) {
    definingClass("Lcom/laurencedawson/reddit_sync/singleton/a;")
    returnType { equals("Ljava/util/HashMap;") }
}
