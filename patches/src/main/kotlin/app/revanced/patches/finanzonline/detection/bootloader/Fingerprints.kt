package app.revanced.patches.finanzonline.detection.bootloader

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

// Located @ at.gv.bmf.bmf2go.taxequalization.tools.utils.AttestationHelper#isBootStateOk (3.0.1)
internal val bootStateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.NEW_ARRAY,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.APUT_OBJECT,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.SGET_OBJECT,
        Opcode.IF_EQ,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.GOTO,
        Opcode.MOVE,
        Opcode.RETURN
    )
}

// Located @ at.gv.bmf.bmf2go.taxequalization.tools.utils.AttestationHelper#createKey (3.0.1)
internal val createKeyFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    strings("attestation", "SHA-256", "random", "EC", "AndroidKeyStore")
}