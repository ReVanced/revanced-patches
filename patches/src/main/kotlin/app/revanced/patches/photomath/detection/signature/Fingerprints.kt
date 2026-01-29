package app.revanced.patches.photomath.detection.signature

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.checkSignatureMethodMatch by composingFirstMethod("SHA") {
    instructions(
        Opcode.CONST_STRING(),
        Opcode.INVOKE_STATIC(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.INVOKE_VIRTUAL(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT(),
    )
}
