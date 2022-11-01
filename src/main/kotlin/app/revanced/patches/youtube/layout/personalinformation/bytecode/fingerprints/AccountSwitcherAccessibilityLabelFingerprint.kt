package app.revanced.patches.youtube.layout.personalinformation.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.personalinformation.annotations.HideEmailAddressCompatibility
import app.revanced.patches.youtube.layout.personalinformation.resource.patch.HideEmailAddressResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("account-switcher-accessibility-label-fingerprint")
@HideEmailAddressCompatibility
@Version("0.0.1")
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