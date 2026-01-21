package app.revanced.patches.photomath.misc.annoyances

import app.revanced.patcher.*
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.hideUpdatePopupMethod by gettingFirstMutableMethodDeclaratively {
    definingClass("Lcom/microblink/photomath/main/activity/MainActivity;")
    accessFlags(AccessFlags.FINAL, AccessFlags.PUBLIC)
    returnType("V")
    instructions(
        Opcode.CONST_HIGH16(),
        Opcode.INVOKE_VIRTUAL(), // ViewPropertyAnimator.alpha(1.0f)
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.CONST_WIDE_16(),
        Opcode.INVOKE_VIRTUAL(), // ViewPropertyAnimator.setDuration(1000L)
    )
}
