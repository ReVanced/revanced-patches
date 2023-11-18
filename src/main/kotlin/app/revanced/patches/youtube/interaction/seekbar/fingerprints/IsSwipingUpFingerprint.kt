package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object IsSwipingUpFingerprint : MethodFingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/view/MotionEvent;", "J"),
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT
    )
)