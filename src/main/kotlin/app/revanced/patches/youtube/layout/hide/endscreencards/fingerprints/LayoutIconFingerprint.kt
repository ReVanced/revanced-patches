package app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.layoutIcon
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val layoutIconFingerprint = methodFingerprint {
    returns("Landroid/view/View;")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,

    )
    literal { layoutIcon }
}
