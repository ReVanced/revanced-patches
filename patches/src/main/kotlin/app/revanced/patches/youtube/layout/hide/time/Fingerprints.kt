package app.revanced.patches.youtube.layout.hide.time

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.accessFlags
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.timeCounterMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    instructions(
        Opcode.SUB_LONG_2ADDR(),
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            returnType = "Ljava/lang/CharSequence;",
            location = MatchAfterImmediately(),
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterImmediately()),
        fieldAccess(opcode = Opcode.IGET_WIDE, type = "J", location = MatchAfterImmediately()),
        fieldAccess(opcode = Opcode.IGET_WIDE, type = "J", location = MatchAfterImmediately()),
        opcode(Opcode.SUB_LONG_2ADDR, location = MatchAfterImmediately()),

        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            returnType = "Ljava/lang/CharSequence;",
            location = MatchAfterWithin(5),
        ),
    )
}
