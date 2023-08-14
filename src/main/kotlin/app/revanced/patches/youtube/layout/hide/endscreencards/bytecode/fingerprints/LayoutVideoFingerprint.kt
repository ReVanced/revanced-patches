package app.revanced.patches.youtube.layout.hide.endscreencards.bytecode.fingerprints

import app.revanced.patches.youtube.layout.hide.endscreencards.resource.patch.HideEndscreenCardsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object LayoutVideoFingerprint : LiteralValueFingerprint(
    returnType = "Landroid/view/View;",
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    ),
    literal = HideEndscreenCardsResourcePatch.layoutVideo
)