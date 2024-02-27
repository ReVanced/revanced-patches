package app.revanced.patches.youtube.player.endscreencards.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.EndScreenElementLayoutIcon
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object LayoutIconFingerprint : LiteralValueFingerprint(
    returnType = "Landroid/view/View;",
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    ),
    literalSupplier = { EndScreenElementLayoutIcon }
)