package app.revanced.patches.music.layout.compactheader

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideCategoryBar = bytecodePatch(
    name = "Hide category bar",
    description = "Hides the category bar at the top of the homepage.",
    use = false,
) {
    compatibleWith("com.google.android.apps.youtube.music")

    execute {
        constructCategoryBarFingerprint.method.apply {
            val insertIndex = constructCategoryBarFingerprint.instructionMatches.first().index
            val register = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
            val freeRegister = findFreeRegister(insertIndex, register)

            addInstructions(
                insertIndex,
                """
                    const/16 v$freeRegister, 0x8
                    invoke-virtual { v$register, v$freeRegister }, Landroid/view/View;->setVisibility(I)V
                """
            )
        }
    }
}
