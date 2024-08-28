package app.revanced.patches.soundcloud.ad

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val interceptFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("L")
    parameters("L")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
    )
    strings("SC-Mob-UserPlan", "Configuration")
    custom { _, classDef ->
        classDef.sourceFile == "ApiUserPlanInterceptor.java"
    }
}

internal val userConsumerPlanConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    parameters(
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/String;",
        "Ljava/util/List;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
    )
    custom { _, classDef ->
        classDef.sourceFile == "UserConsumerPlan.kt"
    }
}
