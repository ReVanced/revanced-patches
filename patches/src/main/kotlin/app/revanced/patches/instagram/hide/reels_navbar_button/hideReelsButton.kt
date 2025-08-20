package app.revanced.patches.instagram.hide.reels_navbar_button

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideReelsNavbarButton = bytecodePatch(
    name = "Hide navigation bar Reels button",
) {
    compatibleWith("com.instagram.android")

    execute {
        tabCreateButtonsFingerprint.let {
            val endIndex = it.patternMatch!!.endIndex

            it.method.addInstructionsWithLabels(
                endIndex - 1,
                """
                    const v1, 0x3
                    if-eq v1,v8, :skipButton
                    :skipButton
                    nop
                """
            )
        }
    }
}