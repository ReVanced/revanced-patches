package app.revanced.patches.youtube.player.speedoverlay.fingerprints

import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

/**
 * This value disables 'Playing at 2x speed' while holding down.
 */
object SpeedOverlayFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    parameters = emptyList(),
    opcodes = listOf(Opcode.MOVE_RESULT),
    literalSupplier = { 45411330 }
)