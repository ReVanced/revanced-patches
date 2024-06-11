package app.revanced.patches.youtube.interaction.seekbar

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val doubleSpeedSeekNoticeFingerprint = methodFingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411330 }
}

internal val isSwipingUpFingerprint = methodFingerprint {
    returns("Z")
    parameters("Landroid/view/MotionEvent;", "J")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT,
    )
}

internal val onTouchEventHandlerFingerprint = methodFingerprint(fuzzyPatternScanThreshold = 3) {
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
    custom { methodDef, _ -> methodDef.name == "onTouchEvent" }
}

internal val seekbarTappingFingerprint = methodFingerprint {
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
    custom { methodDef, _ ->
        if (methodDef.name != "onTouchEvent") return@custom false

        methodDef.implementation!!.instructions.any { instruction ->
            if (instruction.opcode != Opcode.CONST) return@any false

            val literal = (instruction as NarrowLiteralInstruction).narrowLiteral

            // onTouchEvent method contains a CONST instruction
            // with this literal making it unique with the rest of the properties of this fingerprint.
            literal == Integer.MAX_VALUE
        }
    }
}

internal val slideToSeekFingerprint = methodFingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411329 }
}
