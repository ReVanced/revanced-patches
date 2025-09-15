package app.revanced.patches.music.interaction.permanentrepeat

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference

@Suppress("unused")
val permanentRepeatPatch = bytecodePatch(
    name = "Permanent repeat",
    description = "Permanently remember your repeating preference even if the playlist ends or another track is played.",
    use = false,
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52"
        )
    )

    execute {
        addResources("music", "interaction.permanentrepeat.permanentRepeatPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_music_play_permanent_repeat"),
        )

        val startIndex = repeatTrackFingerprint.patternMatch!!.endIndex
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
