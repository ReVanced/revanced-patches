package app.revanced.patches.youtube.misc.backgroundplayback

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.backgroundPlaybackManagerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes("L")
    opcodes(
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET,
        Opcode.AND_INT_LIT16,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.CONST,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.GOTO,
        Opcode.SGET_OBJECT,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
    )
}

internal val BytecodePatchContext.backgroundPlaybackSettingsMethod by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes()
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IF_NEZ,
        Opcode.GOTO,
    )
    literal { prefBackgroundAndOfflineCategoryId }
}

internal val BytecodePatchContext.kidsBackgroundPlaybackPolicyControllerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("I", "L", "L")
    opcodes(
        Opcode.CONST_4,
        Opcode.IF_NE,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.IGET,
        Opcode.CONST_4,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
    )
    literal { 5 }
}

internal val BytecodePatchContext.backgroundPlaybackManagerShortsMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes("L")
    instructions(151635310L())
}

internal val BytecodePatchContext.shortsBackgroundPlaybackFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45415425L())
}

// Fix 'E/InputDispatcher: Window handle pip_input_consumer has no registered input channel'
internal val BytecodePatchContext.pipInputConsumerFeatureFlagMethodMatch by composingFirstMethod {
    instructions(
        // PiP input consumer feature flag.
        45638483L(),
    )
}
