package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideCategoryBar = bytecodePatch(
    name = "Hide category bar",
    description = "Hides the category bar at the top of the homepage.",
    use = false,
) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "6.45.54",
            "6.51.53",
            "7.01.53",
            "7.02.52",
            "7.03.52",
        ),
    )

    val constructCategoryBarMatch by constructCategoryBarFingerprint()

    execute {
        constructCategoryBarMatch.mutableMethod.apply {
            val insertIndex = constructCategoryBarMatch.patternMatch!!.startIndex
            val register = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

            addInstructions(
                insertIndex,
                """
                    const/16 v2, 0x8
                    invoke-virtual {v$register, v2}, Landroid/view/View;->setVisibility(I)V
                """,
            )
        }
    }
}
