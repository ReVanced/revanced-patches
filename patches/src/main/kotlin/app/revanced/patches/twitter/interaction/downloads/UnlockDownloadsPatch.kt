package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.MatchBuilder
import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.creatingBytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused", "ObjectPropertyName")
val `Unlock downloads` by creatingBytecodePatch(
    description = "Unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.",
) {
    compatibleWith("com.twitter.android")

    apply {
        fun MatchBuilder.patch(getRegisterAndIndex: MatchBuilder.() -> Pair<Int, Int>) {
            val (index, register) = getRegisterAndIndex()
            method.addInstruction(index, "const/4 v$register, 0x1")
        }

        // Allow downloads for non-premium users.
        showDownloadVideoUpsellBottomSheetMethodMatch.patch {
            val checkIndex = indices.first()
            val register = method.getInstruction<OneRegisterInstruction>(checkIndex).registerA

            checkIndex to register
        }

        // Force show the download menu item.
        constructMediaOptionsSheetMethodMatch.patch {
            val showDownloadButtonIndex = method.instructions.lastIndex - 1
            val register = method.getInstruction<TwoRegisterInstruction>(showDownloadButtonIndex).registerA

            showDownloadButtonIndex to register
        }

        // Make GIFs downloadable.
        buildMediaOptionsSheetMethodMatch.let {
            it.method.apply {
                val checkMediaTypeIndex = it.indices.first()
                val checkMediaTypeInstruction = getInstruction<TwoRegisterInstruction>(checkMediaTypeIndex)

                // Treat GIFs as videos.
                addInstructionsWithLabels(
                    checkMediaTypeIndex + 1,
                    """
                        const/4 v${checkMediaTypeInstruction.registerB}, 0x2 # GIF
                        if-eq v${checkMediaTypeInstruction.registerA}, v${checkMediaTypeInstruction.registerB}, :video
                    """,
                    ExternalLabel("video", getInstruction(it.indices.last())),
                )

                // Remove media.isDownloadable check.
                removeInstruction(
                    instructions.first { it.opcode == Opcode.IGET_BOOLEAN }.location.index + 1,
                )
            }
        }
    }
}
