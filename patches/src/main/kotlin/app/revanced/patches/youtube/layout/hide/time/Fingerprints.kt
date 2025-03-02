package app.revanced.patches.youtube.layout.hide.time

import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val timeCounterFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    opcodes(
        Opcode.SUB_LONG_2ADDR,
        Opcode.IGET_WIDE,
        Opcode.SUB_LONG_2ADDR,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_WIDE,
        Opcode.IGET_WIDE,
        Opcode.SUB_LONG_2ADDR
    )
}
