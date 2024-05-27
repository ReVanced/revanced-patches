package app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val miniPlayerDimensionsCalculatorParentFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("F")
    opcodes(
        Opcode.CONST_HIGH16,
        Opcode.ADD_FLOAT_2ADDR,
        null, // Opcode.MUL_FLOAT or Opcode.MUL_FLOAT_2ADDR
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.FLOAT_TO_INT,
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
}
