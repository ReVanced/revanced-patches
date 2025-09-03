package app.revanced.patches.instagram.hide.stories
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val hideReelsPatch = bytecodePatch(
    name = "Hide Stories",
    description = "Hides Stories from the main page.",
    use = false
) {
    compatibleWith("com.instagram.android")
    execute {
        val addStoryMethod = addStoryButton.method
        val addStoryEndIndex = addStoryButton.patternMatch!!.endIndex

        // Remove addView of Story, when it's created.
        addStoryMethod.removeInstruction(addStoryEndIndex)
    }
}