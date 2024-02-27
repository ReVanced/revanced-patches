package app.revanced.patches.youtube.player.suggestactions.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.SuggestedAction
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object SuggestedActionsFingerprint : LiteralValueFingerprint(
    returnType = "V",
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    literalSupplier = { SuggestedAction }
)