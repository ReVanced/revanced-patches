package app.revanced.patches.youtube.layout.hide.time

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val timeCounterFingerprint = fingerprint(
    fuzzyPatternScanThreshold = 1,
) {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    listOf(
        Opcode.SUB_LONG_2ADDR,
        Opcode.IGET_WIDE,
        Opcode.SUB_LONG_2ADDR,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_WIDE,
        Opcode.IGET_WIDE,
        Opcode.SUB_LONG_2ADDR,
    )
}
