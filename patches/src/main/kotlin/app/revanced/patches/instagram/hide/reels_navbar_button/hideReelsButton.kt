package app.revanced.patches.instagram.hide.reels_navbar_button

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel


@Suppress("unused")
val hideReelsNavbarButton = bytecodePatch(
    name = "Hide navigation bar Reels button",
) {
    compatibleWith("com.instagram.android"("393.1.0.50.76"))

    execute {

        bytecodePatch {
            val endIndex = tabCreateButtonsFingerprint.patternMatch!!.endIndex

            val instruction = tabCreateButtonsFingerprint.method.getInstruction(endIndex+1)
            tabCreateButtonsFingerprint.method.addInstructionsWithLabels(endIndex-1,"""
                const v1, 0x3
                if-eq v1,v8, :skipAdd
            """, ExternalLabel("skipAdd", instruction))
        }
    }
}