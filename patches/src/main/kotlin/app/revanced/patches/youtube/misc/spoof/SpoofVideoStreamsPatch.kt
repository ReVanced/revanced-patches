package app.revanced.patches.youtube.misc.spoof

import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.spoof.spoofVideoStreamsPatch
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_03_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_10_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_14_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

val spoofVideoStreamsPatch = spoofVideoStreamsPatch({
    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    dependsOn(
        userAgentClientSpoofPatch,
        settingsPatch,
        versionCheckPatch
    )
}, {
    is_19_34_or_greater
}, {
    // In 20.14 the flag was merged with 20.03 start playback flag.
    is_20_10_or_greater && !is_20_14_or_greater
}, {
    is_20_03_or_greater
}, {
    addResources("youtube", "misc.fix.playback.spoofVideoStreamsPatch")

    PreferenceScreen.MISC.addPreferences(
        PreferenceScreenPreference(
            key = "revanced_spoof_video_streams_screen",
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            preferences = setOf(
                SwitchPreference("revanced_spoof_video_streams"),
                ListPreference(
                    "revanced_spoof_video_streams_client_type",
                    summaryKey = null,
                    tag = "app.revanced.extension.youtube.settings.preference.CustomDialogListPreference"
                ),
                NonInteractivePreference(
                    // Requires a key and title but the actual text is chosen at runtime.
                    key = "revanced_spoof_video_streams_about_android",
                    tag = "app.revanced.extension.youtube.settings.preference.SpoofStreamingDataSideEffectsPreference"
                ),
                ListPreference(
                    key = "revanced_spoof_video_streams_language",
                    summaryKey = null,
                    // Language strings are declared in Setting patch.
                    entriesKey = "revanced_language_entries",
                    entryValuesKey = "revanced_language_entry_values",
                    tag = "app.revanced.extension.shared.settings.preference.SortedListPreference"
                ),
                SwitchPreference("revanced_spoof_video_streams_ios_force_avc"),
                SwitchPreference("revanced_spoof_streaming_data_stats_for_nerds"),
            ),
        ),
    )
})
