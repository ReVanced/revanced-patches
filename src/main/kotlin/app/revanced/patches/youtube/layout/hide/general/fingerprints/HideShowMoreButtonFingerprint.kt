package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patches.youtube.layout.hide.general.expandButtonDownId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val hideShowMoreButtonFingerprint = literalValueFingerprint(
    literalSupplier = { expandButtonDownId },
) {
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
}
