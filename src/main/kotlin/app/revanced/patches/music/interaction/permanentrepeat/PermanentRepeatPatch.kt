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
            "6.45.54",
            "6.51.53",
            "7.01.53",
            "7.02.52",
            "7.03.52",
        ),
    )

    val repeatTrackFingerprintResult by repeatTrackFingerprint()

    execute {
        val startIndex = repeatTrackFingerprintResult.scanResult.patternScanResult!!.endIndex
        val repeatIndex = startIndex + 1

        repeatTrackFingerprintResult.mutableMethod.apply {
            addInstructionsWithLabels(
                startIndex,
                "goto :repeat",
                ExternalLabel("repeat", instructions[repeatIndex]),
            )
        }
    }
}
