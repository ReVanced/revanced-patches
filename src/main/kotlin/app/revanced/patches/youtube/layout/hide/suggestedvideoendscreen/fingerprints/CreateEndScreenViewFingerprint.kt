package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.fingerprints

import app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.sizeAdjustableLiteAutoNavOverlay
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val createEndScreenViewFingerprint = literalValueFingerprint(
    literalSupplier = { sizeAdjustableLiteAutoNavOverlay },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/content/Context;")
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST,
    )
}
