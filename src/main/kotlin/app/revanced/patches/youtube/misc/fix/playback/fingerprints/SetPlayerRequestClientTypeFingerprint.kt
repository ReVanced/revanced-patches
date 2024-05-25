package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object SetPlayerRequestClientTypeFingerprint : LiteralValueFingerprint(
    opcodes = listOf(
        Opcode.IGET,
        Opcode.IPUT, // Sets ClientInfo.clientId.
    ),
    strings = listOf("10.29"),
    literalSupplier = { 134217728 }
)
