package app.revanced.patches.youtube.layout.hide.breakingnews.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hide.breakingnews.resource.patch.BreakingNewsResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

object BreakingNewsFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IPUT_OBJECT,
    ),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any { instruction ->
            instruction.opcode.ordinal == Opcode.CONST.ordinal &&
            (instruction as? WideLiteralInstruction)?.wideLiteral == BreakingNewsResourcePatch.horizontalCardListId
        } == true
    }
)