package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val doubleSpeedSeekNoticeFingerprint = literalValueFingerprint(
    literalSupplier = { 45411330 }
) {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
}
