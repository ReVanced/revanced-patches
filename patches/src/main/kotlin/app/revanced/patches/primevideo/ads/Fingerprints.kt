package app.revanced.patches.primevideo.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val enterServerInsertedAdBreakStateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    parameters("Lcom/amazon/avod/fsm/Trigger;")
    returns("V")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.CONST_4
    )
    custom { method, classDef ->
        method.name == "enter" && classDef.type == "Lcom/amazon/avod/media/ads/internal/state/ServerInsertedAdBreakState;"
    }
}

internal val doTriggerFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED)
    returns("V")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID
    )
    custom { method, classDef ->
        method.name == "doTrigger" && classDef.type == "Lcom/amazon/avod/fsm/StateBase;"
    }
}