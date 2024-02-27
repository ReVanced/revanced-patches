package app.revanced.patches.youtube.player.endscreencards.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.EndScreenElementLayoutVideo
import app.revanced.util.fingerprint.LiteralValueFingerprint
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
    literalSupplier = { EndScreenElementLayoutVideo }
)