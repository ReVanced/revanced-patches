package app.revanced.patches.music.utils.returnyoutubedislike.fingerprints

import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.ButtonIconPaddingMedium
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object TextComponentFingerprint : LiteralValueFingerprint(
    returnType = "V",
    opcodes = listOf(Opcode.CONST_HIGH16),
    literalSupplier = { ButtonIconPaddingMedium }
)