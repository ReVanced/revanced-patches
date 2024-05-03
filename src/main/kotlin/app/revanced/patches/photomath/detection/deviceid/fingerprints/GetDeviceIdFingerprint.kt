package app.revanced.patches.photomath.detection.deviceid.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object GetDeviceIdFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_NEZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
    ),
    parameters = listOf()
)