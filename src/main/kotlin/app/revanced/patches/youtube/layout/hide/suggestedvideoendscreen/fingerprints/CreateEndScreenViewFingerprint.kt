package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.DisableSuggestedVideoEndScreenResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object CreateEndScreenViewFingerprint : LiteralValueFingerprint(
    returnType= "Landroid/view/View;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/content/Context;"),
    opcodes = listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST
    ),
    literalSupplier = { DisableSuggestedVideoEndScreenResourcePatch.sizeAdjustableLiteAutoNavOverlay }
)