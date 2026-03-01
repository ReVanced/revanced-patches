package app.revanced.patches.youtube.video.playerresponse

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For targets 20.46 and later.
 */
internal val BytecodePatchContext.playerParameterBuilderMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes(
        "Ljava/lang/String;", // VideoId.
        "[B",
        "Ljava/lang/String;", // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "Z",
        "I",
        "L",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video ID is being opened or is currently playing.
        "Z",
        "Z",
        "Lj$/time/Duration;",
    )
}

/**
 * For targets 20.26 and later.
 */
internal val BytecodePatchContext.playerParameterBuilder2026Method by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes(
        "Ljava/lang/String;", // VideoId.
        "[B",
        "Ljava/lang/String;", // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "Z",
        "I",
        "L",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video ID is being opened or is currently playing.
        "Z",
        "Z",
        "Lj$/time/Duration;",
    )
    instructions("psps"())
}

/**
 * For targets 20.15 to 20.25
 */
internal val BytecodePatchContext.playerParameterBuilder2015Method by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes(
        "Ljava/lang/String;", // VideoId.
        "[B",
        "Ljava/lang/String;", // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "Z",
        "I",
        "L",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video ID is being opened or is currently playing.
        "Z",
        "Z",
    )
    instructions("psps"())
}

/**
 * For targets 20.10 to 20.14.
 */
internal val BytecodePatchContext.playerParameterBuilder2010Method by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes(
        "Ljava/lang/String;", // VideoId.
        "[B",
        "Ljava/lang/String;", // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "Z",
        "I",
        "L",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video ID is being opened or is currently playing.
        "Z",
        "Z",
        "Z",
    )
    instructions("psps"())
}

/**
 * For targets 20.02 to 20.09.
 */
internal val BytecodePatchContext.playerParameterBuilder2002Method by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes(
        "Ljava/lang/String;", // VideoId.
        "[B",
        "Ljava/lang/String;", // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "I",
        "L", // 19.25+ parameter
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video ID is being opened or is currently playing.
        "Z",
        "Z",
        "Z",
    )
    instructions("psps"())
}

/**
 * For targets 19.25 to 19.50.
 */
internal val BytecodePatchContext.playerParameterBuilder1925Method by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes(
        "Ljava/lang/String;", // VideoId.
        "[B",
        "Ljava/lang/String;", // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "I",
        "L", // 19.25+ parameter
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video ID is being opened or is currently playing.
        "Z",
        "Z",
    )
    instructions("psps"())
}

/**
 * For targets 19.01 to 19.24.
 */
internal val BytecodePatchContext.playerParameterBuilderLegacyMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes(
        "Ljava/lang/String;", // VideoId.
        "[B",
        "Ljava/lang/String;", // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "I",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video ID is being opened or is currently playing.
        "Z",
        "Z",
    )
}
