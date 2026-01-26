package app.revanced.patches.music.interaction.permanentrepeat

import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.util.findFreeRegister

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/PermanentRepeatPatch;"

@Suppress("unused", "ObjectPropertyName")
val `Permanent repeat` by creatingBytecodePatch(
    description = "Adds an option to always repeat even if the playlist ends or another track is played.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52",
        ),
    )

    apply {
        addResources("music", "interaction.permanentrepeat.permanentRepeatPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_music_play_permanent_repeat"),
        )

        repeatTrackMethodMatch.method.apply {
            val startIndex = repeatTrackMethodMatch.indices.last()
            val repeatIndex = startIndex + 1

            // Start index is at a branch, but the same
            // register is clobbered in both branch paths.
            val freeRegister = findFreeRegister(startIndex + 1)

            addInstructionsWithLabels(
                startIndex,
                """
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->permanentRepeat()Z
                    move-result v$freeRegister
                    if-nez v$freeRegister, :repeat 
                """,
                ExternalLabel("repeat", instructions[repeatIndex]),
            )
        }
    }
}
