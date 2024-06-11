package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.general.expandButtonDownId
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val hideShowMoreButtonFingerprint = methodFingerprint {
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { expandButtonDownId }
}
