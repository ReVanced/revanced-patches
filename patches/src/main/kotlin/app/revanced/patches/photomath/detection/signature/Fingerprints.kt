package app.revanced.patches.photomath.detection.signature

import app.revanced.patcher.allOf
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import com.android.tools.smali.dexlib2.Opcode

internal val checkSignatureMethodMatch = firstMethodComposite("SHA") {
    instructions(
        allOf(Opcode.CONST_STRING(), "SHA"()),
        Opcode.INVOKE_STATIC(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.INVOKE_VIRTUAL(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT(),
    )
}
