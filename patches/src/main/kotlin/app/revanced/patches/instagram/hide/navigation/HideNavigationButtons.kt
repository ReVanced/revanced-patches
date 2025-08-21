package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val hideNavigationButtonsPatch = bytecodePatch(
    name = "Hide navigation buttons",
    description = "Hides navigation bar buttons, such as Reels and the create button.",
    use = false
) {
    compatibleWith("com.instagram.android")

    val hideReels by booleanOption(
        key = "hideReels",
        default = true,
        title = "Hide Reels"
    )

    val hideCreate by booleanOption(
        key = "hideCreate",
        default = true,
        title = "Hide Create"
    )
    
    execute {
        tabCreateButtonsFingerprint.let {
            it.method.apply {
                // Check the current loop index, and skip over adding the navigation button view
                // if the index matches   is for a given button.
                val startIndex = it.patternMatch!!.startIndex
                val endIndex = it.patternMatch!!.endIndex
                val insertIndex = startIndex + 1
                val loopIndexRegister = getInstruction<TwoRegisterInstruction>(startIndex).registerB
                val freeRegister = findFreeRegister(insertIndex, loopIndexRegister)
                val instruction = getInstruction(endIndex + 1)

                addInstructionsWithLabels(
                    insertIndex,
                    """
                        const v$freeRegister, 0x3
                        if-eq v$freeRegister, v$loopIndexRegister, :skipButton
                    """,
                    ExternalLabel("skipButton", instruction)
                )
            }
        }
    }
}
