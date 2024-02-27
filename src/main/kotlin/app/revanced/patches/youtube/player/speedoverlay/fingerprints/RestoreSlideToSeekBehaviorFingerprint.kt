package app.revanced.patches.youtube.player.speedoverlay.fingerprints

import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

/**
 * This value restores the 'Slide to seek' behavior.
 */
object RestoreSlideToSeekBehaviorFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    parameters = emptyList(),
    opcodes = listOf(Opcode.MOVE_RESULT),
    literalSupplier = { 45411329 }
)