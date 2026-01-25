package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.accessFlags
import app.revanced.patcher.after
import app.revanced.patcher.checkCast
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val scrollPositionMethodMatch = firstMethodComposite("scroll_position") {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    opcodes(
        Opcode.IF_NEZ,
        Opcode.INVOKE_DIRECT,
        Opcode.RETURN_VOID,
    )
}

internal val recyclerViewTopScrollingMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        methodCall(smali = "Ljava/util/Iterator;->next()Ljava/lang/Object;"),
        after(Opcode.MOVE_RESULT_OBJECT()),
        checkCast("Landroid/support/v7/widget/RecyclerView;", MatchAfterImmediately()),
        literal(0, after()),
        methodCall(definingClass = "Landroid/support/v7/widget/RecyclerView;", after()),
        after(Opcode.GOTO()),
    )
}
