package app.revanced.patches.youtube.layout.hide.personalinformation.bytecode.fingerprints

import app.revanced.util.patch.LiteralValueFingerprint
import app.revanced.patches.youtube.layout.hide.personalinformation.resource.patch.HideEmailAddressResourcePatch
import com.android.tools.smali.dexlib2.Opcode

object AccountSwitcherAccessibilityLabelFingerprint : LiteralValueFingerprint(
    returnType = "V",
    parameters = listOf("L", "Ljava/lang/Object;"),
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.NEW_ARRAY,
        Opcode.CONST_4,
        Opcode.APUT_OBJECT,
        Opcode.CONST,
    ),
    literal = HideEmailAddressResourcePatch.accountSwitcherAccessibilityLabelId
)