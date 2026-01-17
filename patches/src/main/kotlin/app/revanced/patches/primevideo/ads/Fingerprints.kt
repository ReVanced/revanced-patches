package app.revanced.patches.primevideo.ads

import app.revanced.patcher.*
import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.enterServerInsertedAdBreakStateMethod by gettingFirstMutableMethodDeclaratively {
    name("enter")
    definingClass("Lcom/amazon/avod/media/ads/internal/state/ServerInsertedAdBreakState;")
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes("Lcom/amazon/avod/fsm/Trigger;")
    returnType("V")
    instructions(
        Opcode.INVOKE_VIRTUAL(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.CONST_4(),
        Opcode.CONST_4()
    )
}

internal val BytecodePatchContext.doTriggerMethod by gettingFirstMutableMethodDeclaratively {
    name("doTrigger")
    definingClass("Lcom/amazon/avod/fsm/StateBase;")
    accessFlags(AccessFlags.PROTECTED)
    returnType("V")
    instructions(
        Opcode.IGET_OBJECT(),
        Opcode.INVOKE_INTERFACE(),
        Opcode.RETURN_VOID()
    )
}
