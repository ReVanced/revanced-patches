package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.checkCast
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal val abUriParserFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/Object;")
    parameters("Ljava/lang/Object;")
    instructions(
        methodCall(smali = "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;"),
        fieldAccess(
            definingClass = "Lcom/google/protos/youtube/api/innertube/WebviewEndpointOuterClass${'$'}WebviewEndpoint;",
            name = "webviewEndpoint"
        ),
        checkCast("Lcom/google/protos/youtube/api/innertube/WebviewEndpointOuterClass${'$'}WebviewEndpoint;"),
        methodCall(smali = "Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;")
    )
}

internal val httpUriParserFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Landroid/net/Uri;")
    parameters("Ljava/lang/String;")
    instructions(
        methodCall(smali = "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;"),
        string("https"),
        string("://"),
        string("https:"),
    )
}

