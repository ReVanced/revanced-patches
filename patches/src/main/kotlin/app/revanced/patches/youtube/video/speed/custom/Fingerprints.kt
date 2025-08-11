package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal val getOldPlaybackSpeedsFingerprint = fingerprint {
    parameters("[L", "I")
    strings("menu_item_playback_speed")
}

internal val showOldPlaybackSpeedMenuFingerprint = fingerprint {
    literal { speedUnavailableId }
}

internal val showOldPlaybackSpeedMenuExtensionFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "showOldPlaybackSpeedMenu" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val speedArrayGeneratorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("[L")
    parameters("Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;")
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

internal val disableFastForwardNoticeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { method, _ ->
        method.name == "run" && method.indexOfFirstInstruction {
            // In later targets the code is found in different methods with different strings.
            val string = getReference<StringReference>()?.string
            string == "Failed to easy seek haptics vibrate." || string == "search_landing_cache_key"
        } >= 0
    }
}
