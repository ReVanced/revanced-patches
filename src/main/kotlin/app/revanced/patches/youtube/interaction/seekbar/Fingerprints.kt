package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction

internal val doubleSpeedSeekNoticeFingerprint = fingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411330 }
}

internal val isSwipingUpFingerprint = fingerprint {
    returns("Z")
    parameters("Landroid/view/MotionEvent;", "J")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT,
    )
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
        if (method.name != "onTouchEvent") return@custom false

        method.implementation!!.instructions.any { instruction ->
            if (instruction.opcode != Opcode.CONST) return@any false

            val literal = (instruction as NarrowLiteralInstruction).narrowLiteral

            // onTouchEvent method contains a CONST instruction
            // with this literal making it unique with the rest of the properties of this fingerprint.
            literal == Integer.MAX_VALUE
        }
    }
}

internal val slideToSeekFingerprint = fingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411329 }
}
