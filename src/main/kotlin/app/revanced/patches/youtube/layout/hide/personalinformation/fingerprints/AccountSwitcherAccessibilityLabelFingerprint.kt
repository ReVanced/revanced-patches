package app.revanced.patches.youtube.layout.hide.personalinformation.fingerprints

import app.revanced.patches.youtube.layout.hide.personalinformation.HideEmailAddressResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
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
    literalSupplier = { HideEmailAddressResourcePatch.accountSwitcherAccessibilityLabelId }
)