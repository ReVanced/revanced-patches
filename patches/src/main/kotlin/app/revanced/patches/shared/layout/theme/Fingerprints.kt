package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.lithoOnBoundsChangeMethodMatch by composingFirstMethod {
    name("onBoundsChange")
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/graphics/Rect;")

    lateinit var methodDefiningClass: String
    custom {
        methodDefiningClass = definingClass
        true
    }

    instructions(
        allOf(
            Opcode.IPUT_OBJECT(),
            field { type == "Landroid/graphics/Path;" && definingClass == methodDefiningClass },
        ),
        afterAtMost(
            5,
            method { returnType == "Z" && name == "isStateful" && definingClass == methodDefiningClass },
        ),
        afterAtMost(
            5,
            allOf(
                Opcode.IGET_OBJECT(),
                field { type == "Landroid/graphics/Path;" && definingClass == methodDefiningClass },
            ),
        ),
        after(
            method { toString() == "Landroid/graphics/Paint;->setColor(I)V" },
        ),
    )
}
