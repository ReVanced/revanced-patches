package app.revanced.patches.spotify.lite.ondemand

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val onDemandFingerprint by fingerprint {
    returns("L")
    parameters()
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_EQZ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT,
        Opcode.RETURN_OBJECT,
    )
}
