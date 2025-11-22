package app.revanced.patches.youtube.video.playerresponse

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * For targets 20.46 and later.
 */
internal val playerParameterBuilderFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters(
        "Ljava/lang/String;",  // VideoId.
        "[B",
        "Ljava/lang/String;",  // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "Z",
        "I",
        "L",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video id is being opened or is currently playing.
        "Z",
        "Z",
        "Lj\$/time/Duration;"
    )
}

/**
 * For targets 20.26 and later.
 */
internal val playerParameterBuilder2026Fingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters(
        "Ljava/lang/String;",  // VideoId.
        "[B",
        "Ljava/lang/String;",  // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "Z",
        "I",
        "L",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video id is being opened or is currently playing.
        "Z",
        "Z",
        "Lj\$/time/Duration;"
    )
    instructions(
        string("psps")
    )
}

/**
 * For targets 20.15 to 20.25
 */
internal val playerParameterBuilder2015Fingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters(
        "Ljava/lang/String;",  // VideoId.
        "[B",
        "Ljava/lang/String;",  // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "Z",
        "I",
        "L",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video id is being opened or is currently playing.
        "Z",
        "Z",
    )
    instructions(
        string("psps")
    )
}

/**
 * For targets 20.10 to 20.14.
 */
internal val playerParameterBuilder2010Fingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters(
        "Ljava/lang/String;",  // VideoId.
        "[B",
        "Ljava/lang/String;",  // Player parameters proto buffer.
        "Ljava/lang/String;",
        "I",
        "Z",
        "I",
        "L",
        "Ljava/util/Set;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Z", // Appears to indicate if the video id is being opened or is currently playing.
        "Z",
        "Z",
        "Z"
    )
    instructions(
        string("psps")
    )
}

/**
 * For targets 20.02 to 20.09.
 */
internal val playerParameterBuilder2002Fingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters(
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
        "Z", // Appears to indicate if the video id is being opened or is currently playing.
        "Z",
        "Z",
        "Z",
    )
    instructions(
        string("psps"),
    )
}

/**
 * For targets 19.25 to 19.50.
 */
internal val playerParameterBuilder1925Fingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters(
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
        "Z", // Appears to indicate if the video id is being opened or is currently playing.
        "Z",
        "Z",
    )
    instructions(
        string("psps")
    )
}

/**
 * For targets 19.01 to 19.24.
 */
internal val playerParameterBuilderLegacyFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters(
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
        "Z", // Appears to indicate if the video id is being opened or is currently playing.
        "Z",
        "Z",
    )
}
