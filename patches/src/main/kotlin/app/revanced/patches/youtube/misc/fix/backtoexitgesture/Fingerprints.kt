package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.*
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
        method { toString() == "Ljava/util/Iterator;->next()Ljava/lang/Object;" },
        after(Opcode.MOVE_RESULT_OBJECT()),
        after(allOf(Opcode.CHECK_CAST(), type("Landroid/support/v7/widget/RecyclerView;"))),
        after(0L()),
        after(method { definingClass == "Landroid/support/v7/widget/RecyclerView;" }),
        after(Opcode.GOTO()),
    )
}
