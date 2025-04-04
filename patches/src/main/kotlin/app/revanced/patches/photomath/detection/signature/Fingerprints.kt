package app.revanced.patches.photomath.detection.signature

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val checkSignatureFingerprint = fingerprint {
    opcodes(
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
    )
    strings("SHA")
}
