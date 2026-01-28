package app.revanced.patches.tiktok.shared

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getEnterFromMethod by gettingFirstMethodDeclaratively {
    definingClass { endsWith("/BaseListFragmentPanel;") }
    returnType("Ljava/lang/String;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Z")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
}

internal val BytecodePatchContext.onRenderFirstFrameMethod by gettingFirstMutableMethodDeclaratively("method_enable_viewpager_preload_duration") {
    definingClass { endsWith("/BaseListFragmentPanel;") }
}
