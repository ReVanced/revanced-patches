package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val getOAuthAccessTokenFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/lang/String")
    strings("access_token")
    custom { method, _ -> method.definingClass == "Lnet/dean/jraw/http/oauth/OAuthData;" }
}
