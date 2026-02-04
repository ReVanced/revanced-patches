package app.revanced.patches.youtube.video.information

import app.revanced.patcher.*
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.youtube.shared.videoQualityChangedMethodMatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import org.stringtemplate.v4.compiler.Bytecode

internal val BytecodePatchContext.createVideoPlayerSeekbarMethod by gettingFirstImmutableMethodDeclaratively {
    returnType("V")
    instructions("timed_markers_width"())
}

internal val BytecodePatchContext.onPlaybackSpeedItemClickMethod by gettingFirstMethodDeclaratively {
    name("onItemClick")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "L", "I", "J")
    instructions(
        allOf(
            Opcode.IGET_OBJECT(),
            field { type == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;" },
        ),
    )
}

internal val BytecodePatchContext.playerControllerSetTimeReferenceMethodMatch by
    composingFirstMethod("Media progress reported outside media playback: ") {
        opcodes(
            Opcode.INVOKE_DIRECT_RANGE,
            Opcode.IGET_OBJECT,
        )
    }

internal val BytecodePatchContext.playVideoCheckVideoStreamingDataResponseMethod by gettingFirstImmutableMethodDeclaratively {
    instructions("playVideo called on player response with no videoStreamingData."())
}

/**
 * Matched using class found in [playVideoCheckVideoStreamingDataResponseMethod].
 */
internal fun ClassDef.getSeekMethod() = firstImmutableMethodDeclaratively {
    instructions("Attempting to seek during an ad"())
}

internal val ClassDef.videoLengthMethodMatch by ClassDefComposing.composingFirstMethod {
    opcodes(
        Opcode.MOVE_RESULT_WIDE,
        Opcode.CMP_LONG,
        Opcode.IF_LEZ,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.GOTO,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
    )
}

/**
 * Matches using class found in [mdxPlayerDirectorSetVideoStageMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getMdxSeekMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes("J", "L")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
    )
    custom {
        // The instruction count is necessary here to avoid matching the relative version
        // of the seek method we're after, which has the same function signature as the
        // regular one, is in the same class, and even has the exact same 3 opcodes pattern.
        instructions.count() == 3
    }
}

internal val BytecodePatchContext.mdxPlayerDirectorSetVideoStageMethod by gettingFirstImmutableMethodDeclaratively {
    instructions("MdxDirector setVideoStage ad should be null when videoStage is not an Ad state "())
}

/**
 * Matches using class found in [mdxPlayerDirectorSetVideoStageMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getMdxSeekRelativeMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    // Return type is boolean up to 19.39, and void with 19.39+.
    parameterTypes("J", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
}

/**
 * Matches using class found in [playVideoCheckVideoStreamingDataResponseMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getSeekRelativeMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    // Return type is boolean up to 19.39, and void with 19.39+.
    parameterTypes("J", "L")
    opcodes(
        Opcode.ADD_LONG_2ADDR,
        Opcode.INVOKE_VIRTUAL,
    )
}

internal val BytecodePatchContext.videoEndMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes("J", "L")
    instructions(
        method { parameterTypes.isEmpty() && returnType == "V" },
        afterAtMost(5, 45368273L()),
        "Attempting to seek when video is not playing"(),
    )
}

/**
 * Matches with the class found in [videoQualityChangedMethodMatch].
 */
internal val ClassDef.playbackSpeedMenuSpeedChangedMethodMatch by ClassDefComposing.composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("L")
    instructions(allOf(Opcode.IGET(), field { type == "F" }))
}

internal val BytecodePatchContext.playbackSpeedClassMethod by gettingFirstMethodDeclaratively(
    "PLAYBACK_RATE_MENU_BOTTOM_SHEET_FRAGMENT",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("L")
    parameterTypes("L")
    opcodes(Opcode.RETURN_OBJECT)
}

internal const val YOUTUBE_VIDEO_QUALITY_CLASS_TYPE =
    "Lcom/google/android/libraries/youtube/innertube/model/media/VideoQuality;"

/**
 * YouTube 20.19 and lower.
 */
internal val BytecodePatchContext.videoQualityLegacyMethod by gettingFirstMethodDeclaratively {
    definingClass(YOUTUBE_VIDEO_QUALITY_CLASS_TYPE)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes(
        "I", // Resolution.
        "Ljava/lang/String;", // Human readable resolution: "480p", "1080p Premium", etc
        "Z",
        "L",
    )
}

internal val BytecodePatchContext.videoQualityMethod by gettingFirstMethodDeclaratively {
    definingClass(YOUTUBE_VIDEO_QUALITY_CLASS_TYPE)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes(
        "I", // Resolution.
        "L",
        "Ljava/lang/String;", // Human readable resolution: "480p", "1080p Premium", etc
        "Z",
        "L",
    )
}

internal val BytecodePatchContext.videoQualitySetterMethod by gettingFirstMethodDeclaratively(
    "menu_item_video_quality",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("[L", "I", "Z")
    opcodes(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_BOOLEAN,
    )
}

/**
 * Matches with the class found in [videoQualitySetterMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getSetVideoQualityMethod() = firstMethodDeclaratively {
    returnType("V")
    parameterTypes("L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
    )
}
