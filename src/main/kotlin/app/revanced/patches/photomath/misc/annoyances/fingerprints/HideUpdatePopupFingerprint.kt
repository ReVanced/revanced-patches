package app.revanced.patches.photomath.misc.annoyances.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object HideUpdatePopupFingerprint : MethodFingerprint(
    customFingerprint = { _, classDef ->
        // The popup is shown only in the main activity
        classDef.type == "Lcom/microblink/photomath/main/activity/MainActivity;"
    },
    opcodes = listOf(
        Opcode.CONST_HIGH16,
        Opcode.INVOKE_VIRTUAL, // ViewPropertyAnimator.alpha(1.0f)
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_WIDE_16,
        Opcode.INVOKE_VIRTUAL, // ViewPropertyAnimator.setDuration(1000L)
    ),
    accessFlags = AccessFlags.FINAL or AccessFlags.PUBLIC,
    returnType = "V",
)
