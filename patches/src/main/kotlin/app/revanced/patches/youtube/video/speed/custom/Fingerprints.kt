package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.newInstance
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode


internal val getOldPlaybackSpeedsFingerprint by fingerprint {
    parameters("[L", "I")
    strings("menu_item_playback_speed")
}

internal val showOldPlaybackSpeedMenuFingerprint by fingerprint {
    instructions(
        resourceLiteral(ResourceType.STRING, "varispeed_unavailable_message")
    )
}

internal val showOldPlaybackSpeedMenuExtensionFingerprint by fingerprint {
    custom { method, _ -> method.name == "showOldPlaybackSpeedMenu" }
}

internal val speedArrayGeneratorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("[L")
    parameters("Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;")
    instructions(
        methodCall(name = "size", returnType = "I"),
        newInstance("Ljava/text/DecimalFormat;"),
        string("0.0#"),
        app.revanced.patcher.literal(7),
        opcode(Opcode.NEW_ARRAY),
        fieldAccess(definingClass = "/PlayerConfigModel;", type = "[F")
    )
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
