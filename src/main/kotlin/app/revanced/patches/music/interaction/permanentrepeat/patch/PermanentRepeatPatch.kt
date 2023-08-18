package app.revanced.patches.music.interaction.permanentrepeat.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.music.annotations.MusicCompatibility
import app.revanced.patches.music.interaction.permanentrepeat.fingerprints.RepeatTrackFingerprint

@Patch(false)
@Name("Permanent repeat")
@Description("Permanently remember your repeating preference even if the playlist ends or another track is played.")
@MusicCompatibility
class PermanentRepeatPatch : BytecodePatch(
    listOf(RepeatTrackFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        RepeatTrackFingerprint.result?.let {
            val startIndex = it.scanResult.patternScanResult!!.endIndex
            val repeatIndex = startIndex + 3

            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    startIndex,
                    "goto :repeat",
                    ExternalLabel("repeat", getInstruction(repeatIndex))
                )
            }
        } ?: throw RepeatTrackFingerprint.toErrorResult()
    }
}
