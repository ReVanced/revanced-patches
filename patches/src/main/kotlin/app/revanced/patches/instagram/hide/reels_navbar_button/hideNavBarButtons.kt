package app.revanced.patches.instagram.hide.reels_navbar_button

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel

@Suppress("unused")
val hideReelsNavbarButton = bytecodePatch(
    name = "Hides navigation bar buttons",
    use = false
) {
    compatibleWith("com.instagram.android")

    val hideReels by booleanOption(
        key = "hideReels",
        default = true,
        title = "Hide Reels",
        required = false,
    )

    val hideCreate by booleanOption(
        key = "hideCreate",
        default = false,
        title = "Hide Create",
        required = false,
    )

    
    execute {
        tabCreateButtonsFingerprint.let {
            val endIndex = it.patternMatch!!.endIndex
            val instruction = it.method.getInstruction(endIndex + 1)

            it.method.addInstructionsWithLabels(
                endIndex - 1,
                """
                    const v1, 0x3
                    if-eq v1,v8, :skipButton
                """,
            ExternalLabel("skipButton", instruction)
            )
        }
    }
}
