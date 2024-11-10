package app.revanced.patches.photomath.detection.deviceid

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val getDeviceIdFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    parameters()
    opcodes(
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
    )
}