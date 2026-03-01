package app.revanced.patches.music.layout.navigationbar

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode


internal val BytecodePatchContext.tabLayoutTextMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        anyOf(
            "FEmusic_search"(), // 8.49 and lower.
            "FEsearch"() // 8.50+
        ),
        // Hide navigation label.
        ResourceType.ID("text1"),
        afterAtMost(
            5,
            method { toString() == "Landroid/view/View;->findViewById(I)Landroid/view/View;" }
        ),
        afterAtMost(
            5,
            allOf(Opcode.CHECK_CAST(), type("Landroid/widget/TextView;"))
        ),
        // Set navigation enum.
        anyOf(
            Opcode.SGET_OBJECT(),
            Opcode.IGET_OBJECT()
        ),
        afterAtMost(5, allOf(Opcode.IGET(), field { type == "I" })),

        afterAtMost(
            5,
            allOf(
                Opcode.INVOKE_STATIC(),
                method { returnType == "L" && parameterTypes.size == 1 && parameterTypes.first() == "I" })
        ),
        after(Opcode.MOVE_RESULT_OBJECT()),
        // Hide navigation buttons.
        method("getVisibility")
    )
}
