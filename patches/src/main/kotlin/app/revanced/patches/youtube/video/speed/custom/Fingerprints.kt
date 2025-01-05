package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val getOldPlaybackSpeedsFingerprint by fingerprint {
    parameters("[L", "I")
    strings("menu_item_playback_speed")
}

internal val showOldPlaybackSpeedMenuFingerprint by fingerprint {
    literal { speedUnavailableId }
}

internal val showOldPlaybackSpeedMenuExtensionFingerprint by fingerprint {
    custom { method, classDef ->
        method.name == "showOldPlaybackSpeedMenu" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val speedArrayGeneratorFingerprint by fingerprint {
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

internal val speedLimiterFingerprint by fingerprint {
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
