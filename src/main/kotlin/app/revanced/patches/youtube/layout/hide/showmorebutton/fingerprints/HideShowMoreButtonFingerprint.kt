package app.revanced.patches.youtube.layout.hide.showmorebutton.fingerprints

import app.revanced.patches.youtube.layout.hide.showmorebutton.HideShowMoreButtonResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object HideShowMoreButtonFingerprint : LiteralValueFingerprint(
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT
    ),
    literalSupplier = { HideShowMoreButtonResourcePatch.expandButtonDownId }
)