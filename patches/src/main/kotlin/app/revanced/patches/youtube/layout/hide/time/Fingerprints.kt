package app.revanced.patches.youtube.layout.hide.time

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.timeCounterMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    instructions(
        Opcode.SUB_LONG_2ADDR(),
        after(allOf(Opcode.INVOKE_STATIC(), method { returnType == "Ljava/lang/CharSequence;" })),
        after(Opcode.MOVE_RESULT_OBJECT()),
        after(allOf(Opcode.IGET_WIDE(), field { type == "J" })),
        after(allOf(Opcode.IGET_WIDE(), field { type == "J" })),
        after(Opcode.SUB_LONG_2ADDR()),
        afterAtMost(5, allOf(Opcode.INVOKE_STATIC(), method { returnType == "Ljava/lang/CharSequence;" })),
    )
}
