package app.revanced.patches.music.account.handle.fingerprints

import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.NamesInactiveAccountThumbnailSize
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object NamesInactiveAccountThumbnailSizeFingerprint : LiteralValueFingerprint(
    returnType = "V",
    parameters = listOf("L", "Ljava/lang/Object;"),
    opcodes = listOf(
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.GOTO,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_EQZ
    ),
    literalSupplier = { NamesInactiveAccountThumbnailSize }
)