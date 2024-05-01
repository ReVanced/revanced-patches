package app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints

import app.revanced.patches.youtube.layout.hide.endscreencards.layoutIcon
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val layoutIconFingerprint = literalValueFingerprint(
    literalSupplier = { layoutIcon },
) {
    returns("Landroid/view/View;")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,

    )
}
