package app.revanced.patches.youtube.layout.hide.breakingnews.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.breakingnews.BreakingNewsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object BreakingNewsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IPUT_OBJECT,
    ),
    literalSupplier = { BreakingNewsResourcePatch.horizontalCardListId }
)