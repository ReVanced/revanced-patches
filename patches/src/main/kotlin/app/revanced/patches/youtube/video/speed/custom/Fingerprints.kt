package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.methodCall
import app.revanced.patcher.newInstance
import app.revanced.patcher.opcode
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getOldPlaybackSpeedsMethod by gettingFirstMethodDeclaratively {
    parameterTypes("[L", "I")
    strings("menu_item_playback_speed")
}

internal val BytecodePatchContext.showOldPlaybackSpeedMenuMethod by gettingFirstMethodDeclaratively {
    instructions(
        ResourceType.STRING("varispeed_unavailable_message"),
    )
}

internal val BytecodePatchContext.showOldPlaybackSpeedMenuExtensionMethod by gettingFirstMethodDeclaratively {
    custom { method, _ -> method.name == "showOldPlaybackSpeedMenu" }
}

internal val BytecodePatchContext.serverSideMaxSpeedFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45719140L(),
    )
}

internal val BytecodePatchContext.speedArrayGeneratorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("[L")
    parameterTypes("Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;")
    instructions(
        methodCall(name = "size", returnType = "I"),
        newInstance("Ljava/text/DecimalFormat;"),
        addString("0.0#"),
        7L(),
        opcode(Opcode.NEW_ARRAY),
        fieldAccess(definingClass = "/PlayerConfigModel;", type = "[F"),
    )
}

/**
 * 20.34+
 */
internal val BytecodePatchContext.speedLimiterMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("F", "Lcom/google/android/libraries/youtube/innertube/model/media/PlayerConfigModel;")
    instructions(
        0.25f(),
        4.0f(),
    )
}

/**
 * 20.33 and lower.
 */
internal val BytecodePatchContext.speedLimiterLegacyMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("F")
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
