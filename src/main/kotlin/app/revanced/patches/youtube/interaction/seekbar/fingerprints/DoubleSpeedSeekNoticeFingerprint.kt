package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

object DoubleSpeedSeekNoticeFingerprint : LiteralValueFingerprint(
    returnType = "Z",
    parameters = emptyList(),
    opcodes = listOf(Opcode.MOVE_RESULT),
    literalSupplier = { 45411330 }
)