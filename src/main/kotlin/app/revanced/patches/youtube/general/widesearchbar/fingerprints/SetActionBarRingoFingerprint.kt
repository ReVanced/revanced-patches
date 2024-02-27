package app.revanced.patches.youtube.general.widesearchbar.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ActionBarRingo
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object SetActionBarRingoFingerprint : LiteralValueFingerprint(
    returnType = "L",
    parameters = listOf("L", "L"),
    opcodes = listOf(
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_STATIC
    ),
    literalSupplier = { ActionBarRingo }
)