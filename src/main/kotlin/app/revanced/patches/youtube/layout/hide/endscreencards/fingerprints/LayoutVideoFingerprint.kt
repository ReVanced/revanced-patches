package app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints

import app.revanced.patches.youtube.layout.hide.endscreencards.HideEndscreenCardsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object LayoutVideoFingerprint : LiteralValueFingerprint(
    returnType = "Landroid/view/View;",
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    ),
    literalSupplier = { HideEndscreenCardsResourcePatch.layoutVideo }
)