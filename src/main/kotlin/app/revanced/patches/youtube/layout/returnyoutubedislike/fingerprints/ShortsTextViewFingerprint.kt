package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object ShortsTextViewFingerprint : MethodFingerprint(
    // 18.29.38 method is public final visibility, but in 18.23.35 and older it's protected final.
    // If 18.23.35 is dropped then accessFlags should be specified here.
    returnType = "V",
    parameters = listOf("L", "L"),
    opcodes = listOf(
        Opcode.INVOKE_SUPER,    // first instruction of method
        Opcode.IF_NEZ,
        Opcode.RETURN_VOID,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.SGET_OBJECT,     // insertion point, must be after constructor call to parent class
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.IF_EQ,
        Opcode.CONST_4,
        Opcode.IF_EQ,
        Opcode.RETURN_VOID,
        Opcode.IGET_OBJECT,     // TextView field
        Opcode.CHECK_CAST,
        Opcode.IGET_BOOLEAN,    // boolean field
    )
)