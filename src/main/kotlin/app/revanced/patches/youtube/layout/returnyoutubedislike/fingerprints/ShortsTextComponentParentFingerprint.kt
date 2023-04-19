package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object ShortsTextComponentParentFingerprint : MethodFingerprint(
    returnType = "V",
    access = AccessFlags.PROTECTED or AccessFlags.FINAL,
    parameters = listOf("L", "L"),
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.GOTO,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.INVOKE_STATIC
    )
)