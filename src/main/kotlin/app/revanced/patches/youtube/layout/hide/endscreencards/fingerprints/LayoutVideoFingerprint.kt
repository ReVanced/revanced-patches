package app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints

import app.revanced.patches.youtube.layout.hide.endscreencards.layoutVideo
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val layoutVideoFingerprint = literalValueFingerprint(
    literalSupplier = { layoutVideo },
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
