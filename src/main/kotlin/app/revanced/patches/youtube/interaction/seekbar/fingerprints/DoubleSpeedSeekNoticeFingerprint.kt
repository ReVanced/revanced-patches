package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val doubleSpeedSeekNoticeFingerprint = methodFingerprint {
    returns("Z")
    parameters()
    opcodes(Opcode.MOVE_RESULT)
    literal { 45411330 }
}
