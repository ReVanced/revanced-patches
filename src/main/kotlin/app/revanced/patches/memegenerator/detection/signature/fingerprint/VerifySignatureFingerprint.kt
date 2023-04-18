package app.revanced.patches.memegenerator.detection.signature.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@FuzzyPatternScanMethod(2)
object VerifySignatureFingerprint : MethodFingerprint(
    returnType = "Z",
    access = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Landroid/app/Activity;"),
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.INVOKE_STATIC,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.SGET_OBJECT,
        Opcode.ARRAY_LENGTH,
        Opcode.IF_GE,
        Opcode.AGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.RETURN,
        Opcode.ADD_INT_LIT8
    ),
)