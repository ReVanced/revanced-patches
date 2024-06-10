package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.sizeAdjustableLiteAutoNavOverlay
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val createEndScreenViewFingerprint = methodFingerprint(
    literal { sizeAdjustableLiteAutoNavOverlay },
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
