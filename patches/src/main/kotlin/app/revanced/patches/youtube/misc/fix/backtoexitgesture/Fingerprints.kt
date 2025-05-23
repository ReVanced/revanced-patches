package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val onBackPressedFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    opcodes(Opcode.RETURN_VOID)
    custom { method, classDef ->
        method.name == "onBackPressed" && classDef.endsWith("MainActivity;")
    }
}

internal val scrollPositionFingerprint by fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IF_NEZ,
        Opcode.INVOKE_DIRECT,
        Opcode.RETURN_VOID
    )
    strings("scroll_position")
}

internal val recyclerViewTopScrollingFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    instructions(
        methodCall(smali = "Ljava/util/Iterator;->next()Ljava/lang/Object;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0),
        checkCast("Landroid/support/v7/widget/RecyclerView;", maxAfter = 0),
        literal(0, maxAfter = 0),
        methodCall(definingClass = "Landroid/support/v7/widget/RecyclerView;", maxAfter = 0),
        opcode(Opcode.GOTO, maxAfter = 0)
    )
}
