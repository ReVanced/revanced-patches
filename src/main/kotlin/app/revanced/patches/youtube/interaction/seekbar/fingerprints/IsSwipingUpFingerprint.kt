package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object IsSwipingUpFingerprint : MethodFingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/view/MotionEvent;", "J"),
    opcodes = listOf(
        Opcode.CONST_4,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL
    )
)