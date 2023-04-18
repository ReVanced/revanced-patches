package app.revanced.patches.memegenerator.misc.pro.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object IsFreeVersionFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/Boolean;",
    access = AccessFlags.PUBLIC or AccessFlags.STATIC,
    strings = listOf("free"),
    parameters = listOf("Landroid/content/Context;"),
    opcodes = listOf(
        Opcode.SGET,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_STRING,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ
    )
)
