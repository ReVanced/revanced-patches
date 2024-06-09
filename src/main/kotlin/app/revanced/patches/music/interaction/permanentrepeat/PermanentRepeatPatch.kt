package app.revanced.patches.music.interaction.permanentrepeat

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.music.interaction.permanentrepeat.fingerprints.repeatTrackFingerprint

@Suppress("unused")
val permanentRepeatPatch = bytecodePatch(
    name = "Permanent repeat",
    description = "Permanently remember your repeating preference even if the playlist ends or another track is played.",
    use = false,
) {
    compatibleWith("com.google.android.apps.youtube.music")

    val repeatTrackResult by repeatTrackFingerprint

    execute {
        repeatTrackResult.let {
            val startIndex = it.scanResult.patternScanResult!!.endIndex
            val repeatIndex = startIndex + 3

            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    startIndex,
                    "goto :repeat",
                    ExternalLabel("repeat", instructions[repeatIndex])
                )
            }
        }
    }
}