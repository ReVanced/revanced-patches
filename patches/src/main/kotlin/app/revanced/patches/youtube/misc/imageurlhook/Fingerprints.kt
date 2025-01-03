package app.revanced.patches.youtube.misc.imageurlhook

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val onFailureFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters(
        "Lorg/chromium/net/UrlRequest;",
        "Lorg/chromium/net/UrlResponseInfo;",
        "Lorg/chromium/net/CronetException;"
    )
    custom { method, _ ->
        method.name == "onFailed"
    }
}

// Acts as a parent fingerprint.
internal val onResponseStartedFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
    strings(
        "Content-Length",
        "Content-Type",
        "identity",
        "application/x-protobuf",
    )
    custom { method, _ ->
        method.name == "onResponseStarted"
    }
}

internal val onSucceededFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
    custom { method, _ ->
        method.name == "onSucceeded"
    }
}

internal const val CRONET_URL_REQUEST_CLASS_DESCRIPTOR = "Lorg/chromium/net/impl/CronetUrlRequest;"

internal val requestFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { _, classDef ->
        classDef.type == CRONET_URL_REQUEST_CLASS_DESCRIPTOR
    }
}

internal val messageDigestImageUrlFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Ljava/lang/String;", "L")
}

internal val messageDigestImageUrlParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    strings("@#&=*+-_.,:!?()/~'%;\$")
}
