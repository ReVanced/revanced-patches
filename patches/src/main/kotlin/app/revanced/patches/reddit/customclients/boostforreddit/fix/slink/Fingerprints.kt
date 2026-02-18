package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.getOAuthAccessTokenMethod by gettingFirstMethodDeclaratively("access_token") {
    definingClass("Lnet/dean/jraw/http/oauth/OAuthData;")
    accessFlags(AccessFlags.PUBLIC)
    returnType("Ljava/lang/String;")
}

internal val BytecodePatchContext.handleNavigationMethod by gettingFirstMethodDeclaratively(
    "android.intent.action.SEARCH",
    "subscription",
    "sort",
    "period",
    "boostforreddit.com/themes"
)
