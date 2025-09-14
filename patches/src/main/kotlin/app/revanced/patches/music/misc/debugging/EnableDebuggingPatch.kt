package app.revanced.patches.music.misc.debugging

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch

val enableDebuggingPatch = bytecodePatch(
    name = "Enable debugging",
    description = "Adds options for debugging.",
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
        // TODO: For test. Change this to real.
        addResources("music", "misc.debugging.enableDebuggingPatch")

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_debug_music_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_debug_music"),
                ),
            ),
        )
    }
}
