package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val unlockDownloadsPatch = bytecodePatch(
    name = "Unlock downloads",
    description = "Unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.",
) {
    compatibleWith("com.twitter.android")

    execute {
        fun Fingerprint.patch(getRegisterAndIndex: Fingerprint.() -> Pair<Int, Int>) {
            val (index, register) = getRegisterAndIndex()
            method.addInstruction(index, "const/4 v$register, 0x1")
        }

        // Allow downloads for non-premium users.
        showDownloadVideoUpsellBottomSheetFingerprint.patch {
            val checkIndex = patternMatch!!.startIndex
            val register = method.getInstruction<OneRegisterInstruction>(checkIndex).registerA

            checkIndex to register
        }

        // Force show the download menu item.
        constructMediaOptionsSheetFingerprint.patch {
            val showDownloadButtonIndex = method.instructions.lastIndex - 1
            val register = method.getInstruction<TwoRegisterInstruction>(showDownloadButtonIndex).registerA

            showDownloadButtonIndex to register
        }

        // Make GIFs downloadable.
        val patternMatch = buildMediaOptionsSheetFingerprint.patternMatch!!
        buildMediaOptionsSheetFingerprint.method.apply {
            val checkMediaTypeIndex = patternMatch.startIndex
            val checkMediaTypeInstruction = getInstruction<TwoRegisterInstruction>(checkMediaTypeIndex)

            // Treat GIFs as videos.
            addInstructionsWithLabels(
                checkMediaTypeIndex + 1,
                """
                        const/4 v${checkMediaTypeInstruction.registerB}, 0x2 # GIF
                        if-eq v${checkMediaTypeInstruction.registerA}, v${checkMediaTypeInstruction.registerB}, :video
                    """,
                ExternalLabel("video", getInstruction(patternMatch.endIndex)),
            )

            // Remove media.isDownloadable check.
            removeInstruction(
                instructions.first { it.opcode == Opcode.IGET_BOOLEAN }.location.index + 1,
            )
        }
    }
}
