package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.getOAuthAccessTokenMethod by gettingFirstMutableMethodDeclaratively("access_token") {
    definingClass("Lnet/dean/jraw/http/oauth/OAuthData;")
    accessFlags(AccessFlags.PUBLIC)
    returnType("Ljava/lang/String;")
}

internal val BytecodePatchContext.handleNavigationMethod by gettingFirstMutableMethodDeclaratively(
    "android.intent.action.SEARCH",
    "subscription",
    "sort",
    "period",
    "boostforreddit.com/themes"
)
