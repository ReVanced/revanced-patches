package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import java.util.logging.Logger

@Suppress("unused")
val hideNavigationButtonsPatch = bytecodePatch(
    name = "Hide navigation buttons",
    description = "Hides navigation bar buttons, such as the Reels and Create button.",
    use = false
) {
    compatibleWith("com.instagram.android")

    val hideReels by booleanOption(
        key = "hideReels",
        default = true,
        title = "Hide Reels",
        description = "Permanently hides the Reels button."
    )

    val hideCreate by booleanOption(
        key = "hideCreate",
        default = true,
        title = "Hide Create",
        description = "Permanently hides the Create button."
    )

    execute {
        if (!hideReels!! && !hideCreate!!) {
            return@execute Logger.getLogger(this::class.java.name).warning(
                "No hide navigation buttons options are enabled. No changes made."
            )
        }

        tabCreateButtonsLoopStartFingerprint.method.apply {
                // Check the current loop index, and skip over adding the
                // navigation button view if the index matches a given button.

                val startIndex = tabCreateButtonsLoopStartFingerprint.patternMatch!!.startIndex
                val endIndex = tabCreateButtonsLoopEndFingerprint.patternMatch!!.endIndex
                val insertIndex = startIndex + 1
                val loopIndexRegister = getInstruction<TwoRegisterInstruction>(startIndex).registerA
                val freeRegister = findFreeRegister(insertIndex, loopIndexRegister)
                val instruction = getInstruction(endIndex - 1)

                var instructions = buildString {
                    if (hideCreate!!) {
                        appendLine(
                            """
                                const v$freeRegister, 0x2
                                if-eq v$freeRegister, v$loopIndexRegister, :skipAddView
                            """
                        )
                    }

                    if (hideReels!!) {
                        appendLine(
                            """
                                const v$freeRegister, 0x3
                                if-eq v$freeRegister, v$loopIndexRegister, :skipAddView
                            """
                        )
                    }
                }

                addInstructionsWithLabels(
                    insertIndex,
                    instructions,
                    ExternalLabel("skipAddView", instruction)
                )
            }
        }
    }

