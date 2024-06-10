package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.general.expandButtonDownId
import com.android.tools.smali.dexlib2.Opcode

internal val hideShowMoreButtonFingerprint = methodFingerprint(
    literal { expandButtonDownId },
) {
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
}
