package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val getOldPlaybackSpeedsFingerprint = fingerprint {
    parameters("[L", "I")
    strings("menu_item_playback_speed")
}

internal val showOldPlaybackSpeedMenuFingerprint = fingerprint {
    literal { speedUnavailableId }
}

internal val showOldPlaybackSpeedMenuExtensionFingerprint = fingerprint {
    custom { method, _ -> method.name == "showOldPlaybackSpeedMenu" }
}

internal val speedArrayGeneratorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("[L")
    parameters("Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;")
    opcodes(
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO_16,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT,
    )
    strings("0.0#")
}

internal val speedLimiterFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("F")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.CONST_HIGH16,
        Opcode.GOTO,
        Opcode.CONST_HIGH16,
        Opcode.CONST_HIGH16,
        Opcode.INVOKE_STATIC,
    )
}
