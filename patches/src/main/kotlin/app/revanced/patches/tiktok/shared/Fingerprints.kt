package app.revanced.patches.tiktok.shared

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getEnterFromMethod by gettingFirstMethodDeclaratively {
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
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/BaseListFragmentPanel;")
    }
}

internal val BytecodePatchContext.onRenderFirstFrameMethod by gettingFirstMethodDeclaratively {
    strings("method_enable_viewpager_preload_duration")
    custom { _, classDef ->
        classDef.endsWith("/BaseListFragmentPanel;")
    }
}
