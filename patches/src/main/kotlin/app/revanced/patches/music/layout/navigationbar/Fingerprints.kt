package app.revanced.patches.music.layout.navigationbar

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val BytecodePatchContext.tabLayoutTextMethodMatch by composingFirstMethod("FEmusic_search") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    opcodes(
        Opcode.IGET,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
    )
    custom {
        containsLiteralInstruction(text1) &&
            indexOfGetVisibilityInstruction(this) >= 0
    }
}

internal fun indexOfGetVisibilityInstruction(method: Method) = method.indexOfFirstInstruction {
    opcode == Opcode.INVOKE_VIRTUAL &&
        getReference<MethodReference>()?.name == "getVisibility"
}
