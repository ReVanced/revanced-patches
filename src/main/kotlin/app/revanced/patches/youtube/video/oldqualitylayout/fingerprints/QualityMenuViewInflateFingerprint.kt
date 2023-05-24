package app.revanced.patches.youtube.video.oldqualitylayout.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.video.oldqualitylayout.patch.OldQualityLayoutResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

object QualityMenuViewInflateFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_SUPER,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any {
            if (it.opcode != Opcode.CONST) return@any false

            val literal = (it as WideLiteralInstruction).wideLiteral

            literal == OldQualityLayoutResourcePatch.videoQualityBottomSheetListFragmentTitle
        } ?: false
    }
)