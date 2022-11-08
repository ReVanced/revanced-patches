package app.revanced.patches.youtube.layout.hideendscreencards.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hideendscreencards.annotations.HideEndscreenCardsCompatibility
import app.revanced.patches.youtube.layout.hideendscreencards.resource.patch.HideEndscreenCardsResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("layout-icon-fingerprint")
@HideEndscreenCardsCompatibility
@Version("0.0.1")
object LayoutIconFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.CONST_4,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    ),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any { instruction ->
            instruction.opcode.ordinal == Opcode.CONST.ordinal &&
                    (instruction as? WideLiteralInstruction)?.wideLiteral == HideEndscreenCardsResourcePatch.layoutIcon
        } == true
    }
)