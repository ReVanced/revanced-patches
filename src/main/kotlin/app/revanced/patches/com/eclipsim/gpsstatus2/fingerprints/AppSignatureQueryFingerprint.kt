package app.revanced.patches.com.eclipsim.gpsstatus2.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object AppSignatureQueryFingerprint : MethodFingerprint(
    strings = listOf(
        "n/a",
        "error",
        "ctx.packageName",
    ),
    parameters = listOf(),
    returnType = "V",
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INT_TO_LONG,
        Opcode.REM_LONG_2ADDR,
        Opcode.SPUT_WIDE,
    )
)
