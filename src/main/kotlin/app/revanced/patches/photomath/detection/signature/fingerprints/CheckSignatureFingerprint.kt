package app.revanced.patches.photomath.detection.signature.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object CheckSignatureFingerprint : MethodFingerprint(
    strings = listOf(
        "currentSignature"
    ),
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
    )
)