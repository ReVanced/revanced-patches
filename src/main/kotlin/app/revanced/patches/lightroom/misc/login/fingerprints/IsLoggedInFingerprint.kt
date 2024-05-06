package app.revanced.patches.lightroom.misc.login.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val isLoggedInFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("Z")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.CONST_4,
        Opcode.GOTO
    )
}
