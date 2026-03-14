package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.getOldPlaybackSpeedsMethod by gettingFirstMethodDeclaratively(
    "menu_item_playback_speed",
) {
    parameterTypes("[L", "I")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getShowOldPlaybackSpeedMenuMethod() = firstMethodDeclaratively {
    instructions(
        ResourceType.STRING("varispeed_unavailable_message"),
    )
}

internal val BytecodePatchContext.showOldPlaybackSpeedMenuExtensionMethod by gettingFirstMethodDeclaratively {
    name("showOldPlaybackSpeedMenu")
}

internal val BytecodePatchContext.serverSideMaxSpeedFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45719140L(),
    )
}

internal val BytecodePatchContext.speedArrayGeneratorMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("[L")
    parameterTypes("L")
    instructions(
        method { name == "size" && returnType == "I" },
        allOf(Opcode.NEW_INSTANCE(), type("Ljava/text/DecimalFormat;")),
        "0.0#"(),
        7L(),
        Opcode.NEW_ARRAY(),
        field { type == "[F" },
    )
}

/**
 * 20.34+
 */
internal val BytecodePatchContext.speedLimiterMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("F", "L")
    instructions(
        "setPlaybackRate"(),
        0.25f.toRawBits().toLong()(),
        4.0f.toRawBits().toLong()(),
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


internal fun BytecodePatchContext.getTapAndHoldSpeedMethodMatch() = firstMethodComposite {
    name("run")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        allOf(
            Opcode.IGET_OBJECT(),
            field { type == "Landroid/os/Handler;" }
        ),
        allOf(
            Opcode.INVOKE_VIRTUAL(),
            method { toString() == "Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V" }
        ),
        allOf(
            Opcode.INVOKE_VIRTUAL(),
            method { returnType == "Z" && parameterTypes.isEmpty() }
        ),
        Opcode.IF_EQZ(),
        allOf(Opcode.IGET_BOOLEAN(), field { type == "Z" }),
        Opcode.IF_NEZ(),
        2.0f.toRawBits().toLong()()
    )
}
