package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val isSwipingUpFingerprint = methodFingerprint {
    returns("Z")
    parameters("Landroid/view/MotionEvent;", "J")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT,
    )
}
