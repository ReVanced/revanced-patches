package app.revanced.patches.youtube.misc.playertype.fingerprint

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val videoStateFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT, // obfuscated parameter field name
    )
}
