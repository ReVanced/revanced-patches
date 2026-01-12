package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.getOAuthAccessTokenMethod by gettingFirstMutableMethodDeclaratively("access_token") {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Ljava/lang/String;")
    definingClass("Lnet/dean/jraw/http/oauth/OAuthData;")
}

internal val BytecodePatchContext.handleNavigationMethod by gettingFirstMutableMethodDeclaratively(
    "android.intent.action.SEARCH",
    "subscription",
    "sort",
    "period",
    "boostforreddit.com/themes"
)
