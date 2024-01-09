package app.revanced.patches.youtube.layout.hide.fullscreenambientmode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object InitializeAmbientModeFingerprint : LiteralValueFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.CONSTRUCTOR or AccessFlags.PUBLIC,
    opcodes = listOf(Opcode.MOVE_RESULT),
    literalSupplier = { 45389368 }
)