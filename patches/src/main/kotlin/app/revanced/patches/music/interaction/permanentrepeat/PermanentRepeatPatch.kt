package app.revanced.patches.music.interaction.permanentrepeat

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel

@Suppress("unused")
val permanentRepeatPatch = bytecodePatch(
    name = "Permanent repeat",
    description = "Permanently remember your repeating preference even if the playlist ends or another track is played.",
    use = false,
) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.16.53",
            "8.05.51"
        )
    )

    execute {
        val startIndex = repeatTrackFingerprint.instructionMatches.last().index
        val repeatIndex = startIndex + 1

        repeatTrackFingerprint.method.apply {
            addInstructionsWithLabels(
                startIndex,
                "goto :repeat",
                ExternalLabel("repeat", instructions[repeatIndex]),
            )
        }
    }
}
