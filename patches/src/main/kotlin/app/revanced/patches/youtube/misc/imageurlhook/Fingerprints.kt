package app.revanced.patches.youtube.misc.imageurlhook

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

context(_: BytecodePatchContext)
internal fun ClassDef.getOnFailureMethod() = firstMethodDeclaratively {
    name("onFailed")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes(
        "Lorg/chromium/net/UrlRequest;",
        "Lorg/chromium/net/UrlResponseInfo;",
        "Lorg/chromium/net/CronetException;",
    )
}

// Acts as a parent method.
internal val BytecodePatchContext.onResponseStartedMethod by gettingFirstMethodDeclaratively(
    "Content-Length",
    "Content-Type",
    "identity",
    "application/x-protobuf",
) {
    name("onResponseStarted")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getOnSucceededMethod() = firstMethodDeclaratively {
    name("onSucceeded")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
}

internal const val CRONET_URL_REQUEST_CLASS_DESCRIPTOR = "Lorg/chromium/net/impl/CronetUrlRequest;"

internal val BytecodePatchContext.requestMethod by gettingFirstMethodDeclaratively {
    definingClass(CRONET_URL_REQUEST_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

context(_: BytecodePatchContext)
internal fun ClassDef.getMessageDigestImageUrlMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("Ljava/lang/String;", "L")
}

internal val BytecodePatchContext.messageDigestImageUrlParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        anyOf(
            string { equals("@#&=*+-_.,:!?()/~'%;$") },
            string { equals("@#&=*+-_.,:!?()/~'%;$[]") }, // 20.38+
        ),
    )
}
