package app.revanced.patches.primevideo.ads

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.enterServerInsertedAdBreakStateMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes("Lcom/amazon/avod/fsm/Trigger;")
    returnType("V")
    instructions(
        Opcode.INVOKE_VIRTUAL(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.CONST_4(),
        Opcode.CONST_4()
    )
    name("enter")
    definingClass("Lcom/amazon/avod/media/ads/internal/state/ServerInsertedAdBreakState;")
}

internal val BytecodePatchContext.doTriggerMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PROTECTED)
    returnType("V")
    instructions(
        Opcode.IGET_OBJECT(),
        Opcode.INVOKE_INTERFACE(),
        Opcode.RETURN_VOID()
    )
    name("doTrigger")
    definingClass("Lcom/amazon/avod/fsm/StateBase;")
}
