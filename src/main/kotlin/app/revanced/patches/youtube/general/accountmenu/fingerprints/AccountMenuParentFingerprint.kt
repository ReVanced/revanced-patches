package app.revanced.patches.youtube.general.accountmenu.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.CompactLink
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object AccountMenuParentFingerprint : LiteralValueFingerprint(
    opcodes = listOf(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    literalSupplier = { CompactLink }
)