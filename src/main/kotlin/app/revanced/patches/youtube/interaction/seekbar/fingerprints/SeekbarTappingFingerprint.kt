package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction


object SeekbarTappingFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        // Insert seekbar tapping instructions here.
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
    ),
    customFingerprint = custom@{ methodDef, _ ->
        if (methodDef.name != "onTouchEvent") return@custom false

        methodDef.implementation!!.instructions.any { instruction ->
            if (instruction.opcode != Opcode.CONST) return@any false

            val literal = (instruction as NarrowLiteralInstruction).narrowLiteral

            // onTouchEvent method contains a CONST instruction
            // with this literal making it unique with the rest of the properties of this fingerprint.
            literal == Integer.MAX_VALUE
        }
    }
)