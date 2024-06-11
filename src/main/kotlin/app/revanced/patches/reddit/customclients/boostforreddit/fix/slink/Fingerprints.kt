package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val getOAuthAccessTokenFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/lang/String")
    strings("access_token")
    custom { method, _ -> method.definingClass == "Lnet/dean/jraw/http/oauth/OAuthData;" }
}

internal val handleNavigationFingerprint = methodFingerprint {
    strings(
        "android.intent.action.SEARCH",
        "subscription",
        "sort",
        "period",
        "boostforreddit.com/themes",
    )
}
