package app.revanced.patches.memegenerator.detection.signature

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val verifySignatureFingerprint = fingerprint(fuzzyPatternScanThreshold = 2) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Z")
    parameters("Landroid/app/Activity;")
    opcodes(
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
    )
}