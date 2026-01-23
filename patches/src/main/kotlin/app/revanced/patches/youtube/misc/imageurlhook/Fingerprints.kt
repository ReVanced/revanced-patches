package app.revanced.patches.youtube.misc.imageurlhook

import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.anyInstruction
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.onFailureMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes(
        "Lorg/chromium/net/UrlRequest;",
        "Lorg/chromium/net/UrlResponseInfo;",
        "Lorg/chromium/net/CronetException;",
    )
    custom { method, _ ->
        method.name == "onFailed"
    }
}

// Acts as a parent fingerprint.
internal val BytecodePatchContext.onResponseStartedMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
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

internal val BytecodePatchContext.onSucceededMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Lorg/chromium/net/UrlRequest;", "Lorg/chromium/net/UrlResponseInfo;")
    custom { method, _ ->
        method.name == "onSucceeded"
    }
}

internal const val CRONET_URL_REQUEST_CLASS_DESCRIPTOR = "Lorg/chromium/net/impl/CronetUrlRequest;"

internal val BytecodePatchContext.requestMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { _, classDef ->
        classDef.type == CRONET_URL_REQUEST_CLASS_DESCRIPTOR
    }
}

internal val BytecodePatchContext.messageDigestImageUrlMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("Ljava/lang/String;", "L")
}

internal val BytecodePatchContext.messageDigestImageUrlParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        anyInstruction(
            addString("@#&=*+-_.,:!?()/~'%;\$"),
            addString("@#&=*+-_.,:!?()/~'%;\$[]"), // 20.38+
        ),
    )
}
