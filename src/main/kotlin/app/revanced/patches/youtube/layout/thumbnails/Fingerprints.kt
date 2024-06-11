package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val messageDigestImageUrlFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("Ljava/lang/String;", "L")
}

internal val messageDigestImageUrlParentFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    strings("@#&=*+-_.,:!?()/~'%;\$")
}

internal val onFailureFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;", "Lorg/chromium/net/CronetException;")
    custom { method, _ ->
        method.name == "onFailed"
    }
}

// Acts as a parent fingerprint.
internal val onResponseStartedFingerprint = methodFingerprint {
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

internal val onSucceededFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
    custom { method, _ ->
        method.name == "onSucceeded"
    }
}

internal const val CRONET_URL_REQUEST_CLASS_DESCRIPTOR = "Lorg/chromium/net/impl/CronetUrlRequest;"

internal val requestFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { method, _ ->
        method.definingClass == CRONET_URL_REQUEST_CLASS_DESCRIPTOR
    }
}
