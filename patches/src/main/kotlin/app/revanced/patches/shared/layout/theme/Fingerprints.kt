package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.accessFlags
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.methodCall
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.lithoOnBoundsChangeMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/graphics/Rect;")
    instructions(
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            definingClass = "this",
            type = "Landroid/graphics/Path;",
        ),

        methodCall(
            definingClass = "this",
            name = "isStateful",
            returnType = "Z",
            afterAtMost(5),
        ),

        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "this",
            type = "Landroid/graphics/Paint",
            afterAtMost(5),
        ),
        methodCall(
            smali = "Landroid/graphics/Paint;->setColor(I)V",
            after(),
        ),
    )
    custom { method, _ ->
        method.name == "onBoundsChange"
    }
}
