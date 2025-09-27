package app.revanced.patches.youtube.layout.hide.time

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val timeCounterFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    instructions(
        opcode(Opcode.SUB_LONG_2ADDR),
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            returnType = "Ljava/lang/CharSequence;",
            maxAfter = 0
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0),
        fieldAccess(opcode = Opcode.IGET_WIDE, type = "J", maxAfter = 0),
        fieldAccess(opcode = Opcode.IGET_WIDE, type = "J", maxAfter = 0),
        opcode(Opcode.SUB_LONG_2ADDR, maxAfter = 0),

        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            returnType = "Ljava/lang/CharSequence;",
            maxAfter = 5
        )
    )
}
