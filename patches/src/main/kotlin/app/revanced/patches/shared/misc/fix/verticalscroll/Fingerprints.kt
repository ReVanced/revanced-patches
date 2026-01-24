package app.revanced.patches.shared.misc.fix.verticalscroll

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val canScrollVerticallyMethodMatch = firstMethodComposite {
    definingClass("SwipeRefreshLayout;"::endsWith)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    opcodes(
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    )
}
