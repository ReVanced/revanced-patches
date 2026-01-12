package app.revanced.patches.photomath.detection.deviceid

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getDeviceIdMethod by gettingFirstMutableMethodDeclaratively {
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        Opcode.SGET_OBJECT(),
        Opcode.IGET_OBJECT(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.IF_NEZ(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.INVOKE_VIRTUAL(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.INVOKE_VIRTUAL(),
    )
}
