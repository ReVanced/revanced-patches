package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.playercontrols.annotation.PlayerControlsCompatibility
import app.revanced.patches.youtube.misc.playercontrols.bytecode.patch.PlayerControlsBytecodePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("bottom-controls-inflate-fingerprint")
@PlayerControlsCompatibility
@Version("0.0.1")
object BottomControlsInflateFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any { instruction ->
            (instruction as? WideLiteralInstruction)?.wideLiteral == PlayerControlsBytecodePatch.bottomUiContainerResourceId
        } == true
    }
)