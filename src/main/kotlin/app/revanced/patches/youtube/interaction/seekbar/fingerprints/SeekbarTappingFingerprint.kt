package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction

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
