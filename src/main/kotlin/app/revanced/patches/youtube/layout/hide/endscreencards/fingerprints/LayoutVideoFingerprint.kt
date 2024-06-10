package app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.endscreencards.layoutVideo
import com.android.tools.smali.dexlib2.Opcode

internal val layoutVideoFingerprint = methodFingerprint(
    literal { layoutVideo },
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
