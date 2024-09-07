package app.revanced.patches.soundcloud.ad.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object InterceptFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC.value,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4
    ),
    strings = listOf("SC-Mob-UserPlan", "Configuration"),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "ApiUserPlanInterceptor.kt"
    },
)
