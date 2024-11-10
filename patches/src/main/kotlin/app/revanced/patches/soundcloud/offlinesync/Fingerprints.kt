package app.revanced.patches.soundcloud.offlinesync

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val downloadOperationsURLBuilderFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String")
    parameters("L", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.FILLED_NEW_ARRAY,
    )
}

internal val downloadOperationsHeaderVerificationFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L")
    opcodes(
        Opcode.CONST_STRING,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_STRING,
    )
    strings("X-SC-Mime-Type", "X-SC-Preset", "X-SC-Quality")
}
