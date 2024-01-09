package app.revanced.patches.messenger.inputfield.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object SwitchMessangeInputEmojiButtonFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L", "Z"),
    strings = listOf("afterTextChanged", "expression_search"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.CONST_STRING,
        Opcode.GOTO,
        Opcode.CONST_STRING,
        Opcode.GOTO
    )
)