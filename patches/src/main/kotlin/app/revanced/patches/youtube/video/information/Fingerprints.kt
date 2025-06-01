package app.revanced.patches.youtube.video.information

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import app.revanced.patches.youtube.shared.newVideoQualityChangedFingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val createVideoPlayerSeekbarFingerprint by fingerprint {
    returns("V")
    instructions(
        string("timed_markers_width"),
    )
}

internal val onPlaybackSpeedItemClickFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L", "I", "J")
    custom { method, _ ->
        method.name == "onItemClick" &&
            method.implementation?.instructions?.find {
                it.opcode == Opcode.IGET_OBJECT &&
                    it.getReference<FieldReference>()!!.type == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"
            } != null
    }
}

internal val playerControllerSetTimeReferenceFingerprint by fingerprint {
    opcodes(Opcode.INVOKE_DIRECT_RANGE, Opcode.IGET_OBJECT)
    strings("Media progress reported outside media playback: ")
}

internal val playerInitFingerprint by fingerprint {
    instructions(
        string("playVideo called on player response with no videoStreamingData."),
    )
}

/**
 * Matched using class found in [playerInitFingerprint].
 */
internal val seekFingerprint by fingerprint {
    instructions(
        string("Attempting to seek during an ad"),
    )
}

internal val videoLengthFingerprint by fingerprint {
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
 * Matches using class found in [mdxPlayerDirectorSetVideoStageFingerprint].
 */
internal val mdxSeekFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("J", "L")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
    )
    custom { methodDef, _ ->
        // The instruction count is necessary here to avoid matching the relative version
        // of the seek method we're after, which has the same function signature as the
        // regular one, is in the same class, and even has the exact same 3 opcodes pattern.
        methodDef.implementation!!.instructions.count() == 3
    }
}

internal val mdxPlayerDirectorSetVideoStageFingerprint by fingerprint {
    instructions(
        string("MdxDirector setVideoStage ad should be null when videoStage is not an Ad state "),
    )
}

/**
 * Matches using class found in [mdxPlayerDirectorSetVideoStageFingerprint].
 */
internal val mdxSeekRelativeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    // Return type is boolean up to 19.39, and void with 19.39+.
    parameters("J", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
}

/**
 * Matches using class found in [playerInitFingerprint].
 */
internal val seekRelativeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    // Return type is boolean up to 19.39, and void with 19.39+.
    parameters("J", "L")
    opcodes(
        Opcode.ADD_LONG_2ADDR,
        Opcode.INVOKE_VIRTUAL,
    )
}

/**
 * Resolves with the class found in [newVideoQualityChangedFingerprint].
 */
internal val playbackSpeedMenuSpeedChangedFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters("L")
    instructions(
        fieldAccess(opcode = Opcode.IGET, type = "F")
    )
}

internal val playbackSpeedClassFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L")
    opcodes(
        Opcode.RETURN_OBJECT
    )
    strings("PLAYBACK_RATE_MENU_BOTTOM_SHEET_FRAGMENT")
}
