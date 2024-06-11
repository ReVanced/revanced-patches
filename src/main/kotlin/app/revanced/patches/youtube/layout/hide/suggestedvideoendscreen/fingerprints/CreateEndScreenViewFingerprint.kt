package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.sizeAdjustableLiteAutoNavOverlay
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val createEndScreenViewFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/content/Context;")
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST,
    )
    literal { sizeAdjustableLiteAutoNavOverlay }
}
