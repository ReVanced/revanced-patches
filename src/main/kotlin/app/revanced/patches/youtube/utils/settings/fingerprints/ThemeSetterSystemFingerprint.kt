package app.revanced.patches.youtube.utils.settings.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.Appearance
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object ThemeSetterSystemFingerprint : LiteralValueFingerprint(
    returnType = "L",
    opcodes = listOf(Opcode.RETURN_OBJECT),
    literalSupplier = { Appearance }
)