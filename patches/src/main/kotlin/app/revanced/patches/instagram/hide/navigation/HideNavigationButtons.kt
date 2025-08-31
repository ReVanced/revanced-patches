package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import java.util.logging.Logger

@Suppress("unused")
val hideNavigationButtonsPatch = bytecodePatch(
    name = "Hide navigation buttons",
    description = "Hides navigation bar buttons, such as Reels and the Create button.",
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
        if (!hideReels!! && !hideCreate!!) {
            return@execute Logger.getLogger(this::class.java.name).warning(
                "No hide navigation buttons options are enabled. No changes made."
            )
        }
        NavbarViewGroupCreationFingerprint.let {
            it.method.apply {

                val endIndex = NavbarViewGroupCreationFingerprint.patternMatch!!.endIndex
                val pixelLengthRegister = getInstruction<TwoRegisterInstruction>(endIndex).registerA




                // Set the input to decide on viewGroup type to 0 pixels, which forces "COMPACT" taskbar
                addInstruction(endIndex+1,"const v$pixelLengthRegister,0x0")

                // Might affect other UI, maybe a good idea to create a clone?
                // Maybe doesn't work on larger devices?
            }
        }


        tabCreateButtonsLoopStartFingerprint.let {
            it.method.apply {
                // Check the current loop index, and skip over adding the
                // navigation button view if the index matches a given button.

                val startIndex = tabCreateButtonsLoopStartFingerprint.patternMatch!!.startIndex
                val endIndex = tabCreateButtonsLoopEndFingerprint.patternMatch!!.endIndex
                val insertIndex = startIndex + 1
                val loopIndexRegister = getInstruction<TwoRegisterInstruction>(startIndex).registerA
                val freeRegister = findFreeRegister(insertIndex, loopIndexRegister)
                val instruction = getInstruction(endIndex - 1)

                var instructions = ""

                if (hideReels!!) {
                    instructions += """
                        const v$freeRegister, 0x3
                        if-eq v$freeRegister, v$loopIndexRegister, :skipAddView
                    """
                }

                if (hideCreate!!) {
                    instructions += """
                        const v$freeRegister, 0x2
                        if-eq v$freeRegister, v$loopIndexRegister, :skipAddView
                    """
                }

                instructions += """
                        const v$freeRegister, 0x6
                        if-eq v$freeRegister, v$loopIndexRegister, :skipAddView
                    """

                addInstructionsWithLabels(
                    insertIndex,
                    instructions,
                    ExternalLabel("skipAddView", instruction)
                )
            }
        }
    }
}
