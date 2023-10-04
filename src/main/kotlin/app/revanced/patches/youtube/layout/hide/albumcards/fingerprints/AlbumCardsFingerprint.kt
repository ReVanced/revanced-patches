package app.revanced.patches.youtube.layout.hide.albumcards.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.albumcards.AlbumCardsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object AlbumCardsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    ),
    literalSupplier = { AlbumCardsResourcePatch.albumCardId }
)