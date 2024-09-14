package app.revanced.patches.soundcloud.ad.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object InterceptFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC.value,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT
    ),
    strings = listOf("chain", "Configuration"),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "ApiUserPlanInterceptor.kt"
    },
)
