package app.revanced.patches.primevideo.ads

import app.revanced.patcher.*
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.enterServerInsertedAdBreakStateMethod by gettingFirstMethodDeclaratively {
    name("enter")
    definingClass("Lcom/amazon/avod/media/ads/internal/state/ServerInsertedAdBreakState;")
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes("Lcom/amazon/avod/fsm/Trigger;")
    returnType("V")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.CONST_4,
    )
}

internal val BytecodePatchContext.doTriggerMethod by gettingFirstMethodDeclaratively {
    name("doTrigger")
    definingClass("Lcom/amazon/avod/fsm/StateBase;")
    accessFlags(AccessFlags.PROTECTED)
    returnType("V")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
}
