package app.revanced.patches.youtube.misc.imageurlhook

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.onFailureMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes(
        "Lorg/chromium/net/UrlRequest;",
        "Lorg/chromium/net/UrlResponseInfo;",
        "Lorg/chromium/net/CronetException;",
    )
    name("onFailed")
}

// Acts as a parent fingerprint.
internal val onResponseStartedMethodMatch = firstMethodComposite(
    "Content-Length",
    "Content-Type",
    "identity",
    "application/x-protobuf",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
    name("onResponseStarted")
}

internal val BytecodePatchContext.onSucceededMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
    name("onSucceeded")
}

internal const val CRONET_URL_REQUEST_CLASS_DESCRIPTOR = "Lorg/chromium/net/impl/CronetUrlRequest;"

internal val BytecodePatchContext.requestMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    definingClass(CRONET_URL_REQUEST_CLASS_DESCRIPTOR)
}

internal val BytecodePatchContext.messageDigestImageUrlMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("Ljava/lang/String;", "L")
}

internal val messageDigestImageUrlParentMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes()
    strings {
        anyOf(
            "@#&=*+-_.,:!?()/~'%;\$",
            "@#&=*+-_.,:!?()/~'%;\$[]", // 20.38+
        )
    }
}
