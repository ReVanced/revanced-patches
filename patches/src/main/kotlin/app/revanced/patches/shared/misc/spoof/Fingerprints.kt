package app.revanced.patches.shared.misc.spoof

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val buildInitPlaybackRequestFingerprint = fingerprint {
    returns("Lorg/chromium/net/UrlRequest\$Builder;")
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT, // Moves the request URI string to a register to build the request with.
    )
    strings(
        "Content-Type",
        "Range",
    )
}

internal val buildPlayerRequestURIFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    opcodes(
        Opcode.INVOKE_VIRTUAL, // Register holds player request URI.
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_OBJECT,
    )
    strings(
        "key",
        "asig",
    )
}

internal val buildRequestFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Lorg/chromium/net/UrlRequest") // UrlRequest; or UrlRequest$Builder;
    custom { methodDef, _ ->
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

        val parameterTypes = methodDef.parameterTypes
        val parameterTypesSize = parameterTypes.size
        (parameterTypesSize == 6 || parameterTypesSize == 7 || parameterTypesSize == 8) &&
                parameterTypes[1] == "Ljava/util/Map;" // URL headers.
                && indexOfNewUrlRequestBuilderInstruction(methodDef) >= 0
    }
}

internal val protobufClassParseByteBufferFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.STATIC)
    returns("L")
    parameters("L", "Ljava/nio/ByteBuffer;")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    custom { method, _ -> method.name == "parseFrom" }
}

internal val createStreamingDataFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IPUT_OBJECT,
    )
    custom { method, classDef ->
        classDef.fields.any { field ->
            field.name == "a" && field.type.endsWith("/StreamingDataOuterClass\$StreamingData;")
        }
    }
}

internal val buildMediaDataSourceFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters(
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

internal const val HLS_CURRENT_TIME_FEATURE_FLAG = 45355374L

internal val hlsCurrentTimeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Z", "L")
    literal {
        HLS_CURRENT_TIME_FEATURE_FLAG
    }
}

internal const val DISABLED_BY_SABR_STREAMING_URI_STRING = "DISABLED_BY_SABR_STREAMING_URI"

internal val mediaFetchEnumConstructorFingerprint = fingerprint {
    returns("V")
    strings(
        "ENABLED",
        "DISABLED_FOR_PLAYBACK",
        DISABLED_BY_SABR_STREAMING_URI_STRING
    )
}

internal val nerdsStatsVideoFormatBuilderFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters("L")
    strings("codecs=\"")
}

internal val patchIncludedExtensionMethodFingerprint = fingerprint {
    returns("Z")
    parameters()
    custom { method, classDef ->
        classDef.type == EXTENSION_CLASS_DESCRIPTOR && method.name == "isPatchIncluded"
    }
}

// Feature flag that turns on Platypus programming language code compiled to native C++.
// This code appears to replace the player config after the streams are loaded.
// Flag is present in YouTube 19.34, but is missing Platypus stream replacement code until 19.43.
// Flag and Platypus code is also present in newer versions of YouTube Music.
internal const val MEDIA_FETCH_HOT_CONFIG_FEATURE_FLAG = 45645570L

internal val mediaFetchHotConfigFingerprint = fingerprint {
    literal { MEDIA_FETCH_HOT_CONFIG_FEATURE_FLAG }
}

// YT 20.10+, YT Music 8.11 - 8.14.
// Flag is missing in YT Music 8.15+, and it is not known if a replacement flag/feature exists.
internal const val MEDIA_FETCH_HOT_CONFIG_ALTERNATIVE_FEATURE_FLAG = 45683169L

internal val mediaFetchHotConfigAlternativeFingerprint = fingerprint {
    literal { MEDIA_FETCH_HOT_CONFIG_ALTERNATIVE_FEATURE_FLAG }
}

// Feature flag that enables different code for parsing and starting video playback,
// but it's exact purpose is not known. If this flag is enabled while stream spoofing
// then videos will never start playback and load forever.
// Flag does not seem to affect playback if spoofing is off.
internal const val PLAYBACK_START_CHECK_ENDPOINT_USED_FEATURE_FLAG = 45665455L

internal val playbackStartDescriptorFeatureFlagFingerprint = fingerprint {
    parameters()
    returns("Z")
    literal { PLAYBACK_START_CHECK_ENDPOINT_USED_FEATURE_FLAG }
}

internal fun indexOfNewUrlRequestBuilderInstruction(method: Method) = method.indexOfFirstInstruction {
    val reference = getReference<MethodReference>()
    opcode == Opcode.INVOKE_VIRTUAL && reference?.definingClass == "Lorg/chromium/net/CronetEngine;"
            && reference.name == "newUrlRequestBuilder"
            && reference.parameterTypes.size == 3
            && reference.parameterTypes[0] == "Ljava/lang/String;"
            && reference.parameterTypes[1] == "Lorg/chromium/net/UrlRequest\$Callback;"
            && reference.parameterTypes[2] == "Ljava/util/concurrent/Executor;"
}
