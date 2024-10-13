package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.fingerprint
import app.revanced.util.containsWideLiteralInstructionValue
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import java.lang.Integer


internal val swipingUpGestureParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    literal { 45379021 }
}

/**
 * Resolves using the class found in [swipingUpGestureParentFingerprint].
 */
internal val showSwipingUpGuideFingerprint = fingerprint {
    accessFlags(AccessFlags.FINAL)
    returns("Z")
    parameters()
    literal { 1 }
}

/**
 * Resolves using the class found in [swipingUpGestureParentFingerprint].
 */
internal val allowSwipingUpGestureFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
}

internal val doubleSpeedSeekNoticeFingerprint = fingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411330 }
}

internal val onTouchEventHandlerFingerprint = fingerprint(fuzzyPatternScanThreshold = 3) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.PUBLIC)
    returns("Z")
    parameters("L")
    opcodes(
        Opcode.INVOKE_VIRTUAL, // nMethodReference
        Opcode.RETURN,
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN,
        Opcode.INT_TO_FLOAT,
        Opcode.INT_TO_FLOAT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL, // oMethodReference
    )
    custom { method, _ -> method.name == "onTouchEvent" }
}

internal val seekbarTappingFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("L")
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        // Insert seekbar tapping instructions here.
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
    )
    custom { method, _ ->
        // onTouchEvent method contains a CONST instruction
        // with this literal making it unique with the rest of the properties of this fingerprint.
        method.name == "onTouchEvent" && method.containsWideLiteralInstructionValue(Integer.MAX_VALUE.toLong())
    }
}

internal val slideToSeekFingerprint = fingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411329 }
}
