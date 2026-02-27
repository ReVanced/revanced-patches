package app.revanced.patches.shared.misc.spoof

import app.revanced.patcher.accessFlags
import app.revanced.patcher.anyField
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method

internal val BytecodePatchContext.buildInitPlaybackRequestMethodMatch by composingFirstMethod("Content-Type", "Range") {
    returnType($$"Lorg/chromium/net/UrlRequest$Builder;")
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT, // Moves the request URI string to a register to build the request with.
    )
}

internal val BytecodePatchContext.buildPlayerRequestURIMethodMatch by composingFirstMethod("key", "asig") {
    returnType("Ljava/lang/String;")
    opcodes(
        Opcode.INVOKE_VIRTUAL, // Register holds player request URI.
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_OBJECT,
    )
}

internal val BytecodePatchContext.buildRequestMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Lorg/chromium/net/UrlRequest") // UrlRequest; or UrlRequest$Builder;
    instructions(
        method("newUrlRequestBuilder"),
    ) // UrlRequest; or UrlRequest$Builder;
    custom {
        // Different targets have slightly different parameters

        // Earlier targets have parameters:
        // L
        // Ljava/util/Map;
        // [B
        // L
        // L
        // L
        // Lorg/chromium/net/UrlRequest$Callback;

        // Later targets have parameters:
        // L
        // Ljava/util/Map;
        // [B
        // L
        // L
        // L
        // Lorg/chromium/net/UrlRequest\$Callback;
        // L

        // 20.16+ uses a refactored and extracted method:
        // L
        // Ljava/util/Map;
        // [B
        // L
        // Lorg/chromium/net/UrlRequest$Callback;
        // L

        val parameterTypes = parameterTypes
        val parameterTypesSize = parameterTypes.size
        (parameterTypesSize == 6 || parameterTypesSize == 7 || parameterTypesSize == 8) &&
            parameterTypes[1] == "Ljava/util/Map;" && // URL headers.
            indexOfNewUrlRequestBuilderInstruction(this) >= 0
    }
}

internal val BytecodePatchContext.protobufClassParseByteBufferMethod by gettingFirstImmutableMethodDeclaratively {
    name("parseFrom")
    accessFlags(AccessFlags.PROTECTED, AccessFlags.STATIC)
    returnType("L")
    parameterTypes("L", "Ljava/nio/ByteBuffer;")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
}

internal val BytecodePatchContext.createStreamingDataMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("L")
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IPUT_OBJECT,
    )
    custom {
        immutableClassDef.anyField { name == "a" && type.endsWith($$"/StreamingDataOuterClass$StreamingData;") }
    }
}

internal val BytecodePatchContext.buildMediaDataSourceMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes(
        "Landroid/net/Uri;",
        "J",
        "I",
        "[B",
        "Ljava/util/Map;",
        "J",
        "J",
        "Ljava/lang/String;",
        "I",
        "Ljava/lang/Object;",
    )
}

internal val BytecodePatchContext.hlsCurrentTimeMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Z", "L")
    instructions(
        45355374L(), // HLS current time feature flag.
    )
}

internal const val DISABLED_BY_SABR_STREAMING_URI_STRING = "DISABLED_BY_SABR_STREAMING_URI"

internal val BytecodePatchContext.mediaFetchEnumConstructorMethodMatch by composingFirstMethod {
    returnType("V")
    instructions(
        "ENABLED"(),
        "DISABLED_FOR_PLAYBACK"(),
        DISABLED_BY_SABR_STREAMING_URI_STRING(),
    )
}

internal val BytecodePatchContext.nerdsStatsVideoFormatBuilderMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes("L")
    instructions(
        "codecs=\""(),
    )
}

internal val BytecodePatchContext.patchIncludedExtensionMethodMethod by gettingFirstMethodDeclaratively {
    name("isPatchIncluded")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    returnType("Z")
    parameterTypes()
}

// Feature flag that turns on Platypus programming language code compiled to native C++.
// This code appears to replace the player config after the streams are loaded.
// Flag is present in YouTube 19.34, but is missing Platypus stream replacement code until 19.43.
// Flag and Platypus code is also present in newer versions of YouTube Music.
internal val BytecodePatchContext.mediaFetchHotConfigMethodMatch by composingFirstMethod {
    instructions(45645570L())
}

// YT 20.10+, YT Music 8.11 - 8.14.
// Flag is missing in YT Music 8.15+, and it is not known if a replacement flag/feature exists.
internal val BytecodePatchContext.mediaFetchHotConfigAlternativeMethodMatch by composingFirstMethod {
    instructions(45683169L())
}

// Feature flag that enables different code for parsing and starting video playback,
// but its exact purpose is not known. If this flag is enabled while stream spoofing
// then videos will never start playback and load forever.
// Flag does not seem to affect playback if spoofing is off.
internal val BytecodePatchContext.playbackStartDescriptorFeatureFlagMethodMatch by composingFirstMethod {
    parameterTypes()
    returnType("Z")
    instructions(45665455L())
}

internal fun indexOfNewUrlRequestBuilderInstruction(method: Method) = method.indexOfFirstInstruction {
    val reference = methodReference ?: return@indexOfFirstInstruction false

    opcode == Opcode.INVOKE_VIRTUAL && reference.definingClass == "Lorg/chromium/net/CronetEngine;" &&
        reference.name == "newUrlRequestBuilder" &&
        reference.parameterTypes.size == 3 &&
        reference.parameterTypes[0] == "Ljava/lang/String;" &&
        reference.parameterTypes[1] == "Lorg/chromium/net/UrlRequest\$Callback;" &&
        reference.parameterTypes[2] == "Ljava/util/concurrent/Executor;"
}
