package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object JRAWgetAccessTokenFingerprint: MethodFingerprint (
    strings = listOf("access_token"),
    accessFlags = AccessFlags.PUBLIC.value,
    returnType = "Ljava/lang/String",
    customFingerprint = { methodDef, _ -> methodDef.definingClass == "Lnet/dean/jraw/http/oauth/OAuthData;" }
)