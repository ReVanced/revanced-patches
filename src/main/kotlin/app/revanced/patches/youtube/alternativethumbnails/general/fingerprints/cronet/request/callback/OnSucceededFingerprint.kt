package app.revanced.patches.youtube.alternativethumbnails.general.fingerprints.cronet.request.callback

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object OnSucceededFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters =  listOf("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "onSucceeded"
    }
)