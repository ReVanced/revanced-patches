package app.revanced.patches.youtube.video.information

import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val createVideoPlayerSeekbarFingerprint = fingerprint {
    returns("V")
    strings("timed_markers_width")
}

internal val onPlaybackSpeedItemClickFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L", "I", "J")
    custom { method, _ ->
        method.name == "onItemClick" && method.implementation?.instructions?.find {
            it.opcode == Opcode.IGET_OBJECT &&
                it.getReference<FieldReference>()!!.type == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"
        } != null
    }
}

internal val playerControllerSetTimeReferenceFingerprint = fingerprint {
    opcodes(Opcode.INVOKE_DIRECT_RANGE, Opcode.IGET_OBJECT)
    strings("Media progress reported outside media playback: ")
}

internal val playerInitFingerprint = fingerprint {
    strings("playVideo called on player response with no videoStreamingData.")
}

internal val seekFingerprint = fingerprint {
    strings("Attempting to seek during an ad")
}

internal val videoLengthFingerprint = fingerprint {
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
