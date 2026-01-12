package app.revanced.patches.photomath.misc.annoyances

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.fingerprint
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.hideUpdatePopupMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.FINAL, AccessFlags.PUBLIC)
    returnType("V")
    definingClass("Lcom/microblink/photomath/main/activity/MainActivity;")
    instructions(
        Opcode.CONST_HIGH16(),
        Opcode.INVOKE_VIRTUAL(), // ViewPropertyAnimator.alpha(1.0f)
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.CONST_WIDE_16(),
        Opcode.INVOKE_VIRTUAL(), // ViewPropertyAnimator.setDuration(1000L)
    )
}
