package app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.layoutCircle
import com.android.tools.smali.dexlib2.Opcode

internal val layoutCircleFingerprint = methodFingerprint(
    literal { layoutCircle },
) {
    returns("Landroid/view/View;")
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
}
