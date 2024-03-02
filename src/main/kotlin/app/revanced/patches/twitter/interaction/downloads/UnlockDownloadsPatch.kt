package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.twitter.interaction.downloads.fingerprints.BuildMediaOptionsSheetFingerprint
import app.revanced.patches.twitter.interaction.downloads.fingerprints.ConstructMediaOptionsSheetFingerprint
import app.revanced.patches.twitter.interaction.downloads.fingerprints.ShowDownloadVideoUpsellBottomSheetFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Unlock downloads",
    description = "Unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")],
)
@Suppress("unused")
object UnlockDownloadsPatch : BytecodePatch(
    setOf(
        ConstructMediaOptionsSheetFingerprint,
        ShowDownloadVideoUpsellBottomSheetFingerprint,
        BuildMediaOptionsSheetFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        fun MethodFingerprint.patch(getRegisterAndIndex: MethodFingerprintResult.() -> Pair<Int, Int>) = result?.let {
            getRegisterAndIndex(it).let { (index, register) ->
                it.mutableMethod.addInstruction(index, "const/4 v$register, 0x1")
            }
        } ?: throw exception

        // Allow downloads for non-premium users.
        ShowDownloadVideoUpsellBottomSheetFingerprint.patch {
            val checkIndex = scanResult.patternScanResult!!.startIndex
            val register = mutableMethod.getInstruction<OneRegisterInstruction>(checkIndex).registerA

            checkIndex to register
        }

        // Force show the download menu item.
        ConstructMediaOptionsSheetFingerprint.patch {
            val showDownloadButtonIndex = mutableMethod.getInstructions().lastIndex - 1
            val register = mutableMethod.getInstruction<TwoRegisterInstruction>(showDownloadButtonIndex).registerA

            showDownloadButtonIndex to register
        }

        // Make GIFs downloadable.
        BuildMediaOptionsSheetFingerprint.result?.let {
            val scanResult = it.scanResult.patternScanResult!!
            it.mutableMethod.apply {
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
                    getInstructions().first { insn -> insn.opcode == Opcode.IGET_BOOLEAN }.location.index + 1,
                )
            }
        } ?: throw BuildMediaOptionsSheetFingerprint.exception
    }
}
