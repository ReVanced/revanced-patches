package app.revanced.patches.youtube.layout.hide.loadmorebutton.bytecode.fingerprints

import app.revanced.patches.youtube.layout.hide.loadmorebutton.resource.patch.HideLoadMoreButtonResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object HideLoadMoreButtonFingerprint : LiteralValueFingerprint(
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT
    ),
    literal = HideLoadMoreButtonResourcePatch.expandButtonDownId
)