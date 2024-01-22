package app.revanced.patches.photomath.misc.updatepopup.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object HideUpdatePopupFingerprint : MethodFingerprint(
    customFingerprint = { _, classDef -> classDef.type == "Lcom/microblink/photomath/main/activity/MainActivity;" }, // The popup is shown only in the main activity
    opcodes = listOf(
        Opcode.CONST_4,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL, // View.setVisibility(0)
        Opcode.IGET_OBJECT,
        Opcode.CONST_HIGH16,
        Opcode.INVOKE_VIRTUAL, // ViewPropertyAnimator.alpha(1.0f)
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_WIDE_16,
        Opcode.INVOKE_VIRTUAL, // ViewPropertyAnimator.setDuration(1000L)
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL, // ViewPropertyAnimator.setListener(new AnimatorListenerAdapter() { ... })
    ),
    accessFlags = AccessFlags.FINAL or AccessFlags.PUBLIC,
    returnType = "V",
)
