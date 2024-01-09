package app.revanced.patches.youtube.misc.settings.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.misc.settings.SettingsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object SetThemeFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "L",
    parameters = listOf(),
    opcodes = listOf(Opcode.RETURN_OBJECT),
    literalSupplier = { SettingsResourcePatch.appearanceStringId }
)
