package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MiniplayerModernConstructorFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("L"),
    literalSupplier = { 45623000L } // Magic number found in the constructor.
)