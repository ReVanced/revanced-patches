package app.revanced.patches.messenger.inputfield.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val switchMessangeInputEmojiButtonFingerprint = methodFingerprint {
    returns("V")
    parameters("L", "Z")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.CONST_STRING,
        Opcode.GOTO,
        Opcode.CONST_STRING,
        Opcode.GOTO
    )
    strings("afterTextChanged", "expression_search")
}