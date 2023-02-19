package app.revanced.patches.youtube.layout.hide.personalinformation.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hide.personalinformation.resource.patch.HideEmailAddressResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

object AccountSwitcherAccessibilityLabelFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.NEW_ARRAY,
        Opcode.CONST_4,
        Opcode.APUT_OBJECT,
        Opcode.CONST,
    ),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any { instruction ->
            instruction.opcode.ordinal == Opcode.CONST.ordinal &&
            (instruction as? WideLiteralInstruction)?.wideLiteral == HideEmailAddressResourcePatch.accountSwitcherAccessibilityLabelId
        } == true
    }
)