package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val lithoOnBoundsChangeFingerprint by fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/graphics/Rect;")
    instructions(
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            definingClass = "this",
            type = "Landroid/graphics/Path;"
        ),

        methodCall(
            definingClass = "this",
            name = "isStateful",
            returnType = "Z",
            maxAfter = 5
        ),

        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "this",
            type = "Landroid/graphics/Paint",
            maxAfter = 5
        ),
        methodCall(
            smali = "Landroid/graphics/Paint;->setColor(I)V",
            maxAfter = 0
        )
    )
    custom { method, _ ->
        method.name == "onBoundsChange"
    }
}
