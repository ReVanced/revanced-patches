package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.accessFlags
import app.revanced.patcher.checkCast
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.scrollPositionMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    opcodes(
        Opcode.IF_NEZ,
        Opcode.INVOKE_DIRECT,
        Opcode.RETURN_VOID,
    )
    strings("scroll_position")
}

internal val BytecodePatchContext.recyclerViewTopScrollingMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        methodCall(smali = "Ljava/util/Iterator;->next()Ljava/lang/Object;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately()),
        checkCast("Landroid/support/v7/widget/RecyclerView;", MatchAfterImmediately()),
        literal(0, location = MatchAfterImmediately()),
        methodCall(definingClass = "Landroid/support/v7/widget/RecyclerView;", location = MatchAfterImmediately()),
        opcode(Opcode.GOTO, MatchAfterImmediately()),
    )
}
