package app.revanced.patches.youtube.layout.buttons.player.flyout.fingerprints

import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object SleepTimerExperimentalFeatureFingerprint : LiteralValueFingerprint(
    opcodes = listOf(
        Opcode.IPUT_BOOLEAN,
        Opcode.RETURN_VOID
    ),
    literalSupplier = { 45640654 }
)
