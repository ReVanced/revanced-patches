package app.revanced.patches.music.layout.minimizedplayback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val kidsMiniPlaybackPolicyControllerFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("I", "L", "Z")
    opcodes(
        Opcode.IGET,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
        Opcode.IF_NE,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQ,
        Opcode.GOTO,
        Opcode.RETURN_VOID,
        Opcode.SGET_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_NE,
        Opcode.IPUT_BOOLEAN,
    )
}
