package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

val enableDebuggingPatch = resourcePatch(
    name = "Enable debugging",
    description = "Adds options for debugging.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith("com.google.android.youtube")

    execute {
        addResources("youtube", "misc.debugging.enableDebuggingPatch")

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_debug_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_debug"),
                    SwitchPreference("revanced_debug_protobuffer"),
                    SwitchPreference("revanced_debug_stacktrace"),
                    SwitchPreference("revanced_debug_toast_on_error"),
                ),
            ),
        )
    }
}
