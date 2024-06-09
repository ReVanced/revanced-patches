package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.twitter.interaction.downloads.fingerprints.buildMediaOptionsSheetFingerprint
import app.revanced.patches.twitter.interaction.downloads.fingerprints.constructMediaOptionsSheetFingerprint
import app.revanced.patches.twitter.interaction.downloads.fingerprints.showDownloadVideoUpsellBottomSheetFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val unlockDownloadsPatch = bytecodePatch(
    name = "Unlock downloads",
    description = "Unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.",
) {
    compatibleWith("com.twitter.android")

    val constructMediaOptionsSheetResult by constructMediaOptionsSheetFingerprint
    val showDownloadVideoUpsellBottomSheetResult by showDownloadVideoUpsellBottomSheetFingerprint
    val buildMediaOptionsSheetResult by buildMediaOptionsSheetFingerprint

    fun MethodFingerprintResult.patch(getRegisterAndIndex: MethodFingerprintResult.() -> Pair<Int, Int>) {
        val (index, register) = getRegisterAndIndex()
        mutableMethod.addInstruction(index, "const/4 v$register, 0x1")
    }

    execute {
        // Allow downloads for non-premium users.
        showDownloadVideoUpsellBottomSheetResult.patch {
            val checkIndex = scanResult.patternScanResult!!.startIndex
            val register = mutableMethod.getInstruction<OneRegisterInstruction>(checkIndex).registerA

            checkIndex to register
        }

        // Force show the download menu item.
        constructMediaOptionsSheetResult.patch {
            val showDownloadButtonIndex = mutableMethod.instructions.lastIndex - 1
            val register = mutableMethod.getInstruction<TwoRegisterInstruction>(showDownloadButtonIndex).registerA

            showDownloadButtonIndex to register
        }

        // Make GIFs downloadable.
        val scanResult = buildMediaOptionsSheetResult.scanResult.patternScanResult!!
        buildMediaOptionsSheetResult.mutableMethod.apply {
            val checkMediaTypeIndex = scanResult.startIndex
            val checkMediaTypeInstruction = getInstruction<TwoRegisterInstruction>(checkMediaTypeIndex)

            // Treat GIFs as videos.
            addInstructionsWithLabels(
                checkMediaTypeIndex + 1,
                """
                        const/4 v${checkMediaTypeInstruction.registerB}, 0x2 # GIF
                        if-eq v${checkMediaTypeInstruction.registerA}, v${checkMediaTypeInstruction.registerB}, :video
                    """,
                ExternalLabel("video", getInstruction(scanResult.endIndex)),
            )

            // Remove media.isDownloadable check.
            removeInstruction(
                instructions.first { it.opcode == Opcode.IGET_BOOLEAN }.location.index + 1,
            )
        }
    }
}
