package app.revanced.patches.finanzonline.detection.bootloader.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Located @ at.gv.bmf.bmf2go.taxequalization.tools.utils.AttestationHelper#isBootStateOk (3.0.1)
object BootStateFingerprint : MethodFingerprint(
    "Z",
    accessFlags = AccessFlags.PUBLIC.value,
    opcodes = listOf(
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
)
