package app.revanced.patches.youtube.layout.alternativethumbnails.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Acts as a parent fingerprint.
object CronetURLRequestCallbackOnResponseStartedFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters =  listOf("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;"),
    strings = listOf(
        "Content-Length",
        "Content-Type",
        "identity",
        "application/x-protobuf"
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "onResponseStarted"
    }
)