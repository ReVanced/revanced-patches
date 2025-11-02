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
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

val spoofVideoStreamsPatch = spoofVideoStreamsPatch(
    extensionClassDescriptor = "Lapp/revanced/extension/youtube/patches/spoof/SpoofVideoStreamsPatch;",
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    fixMediaFetchHotConfig = {
        is_19_34_or_greater
    },
    fixMediaFetchHotConfigAlternative = {
        // In 20.14 the flag was merged with 20.03 start playback flag.
        is_20_10_or_greater && !is_20_14_or_greater
    },
    fixParsePlaybackResponseFeatureFlag = {
        is_20_03_or_greater
    },

    block = {
        compatibleWith(
            "com.google.android.youtube"(
                "19.34.42",
                "20.07.39",
                "20.13.41",
                "20.14.43",
            )
        )

        dependsOn(
            userAgentClientSpoofPatch,
            settingsPatch,
            versionCheckPatch
        )
    },

    executeBlock = {
        addResources("youtube", "misc.fix.playback.spoofVideoStreamsPatch")

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_spoof_video_streams_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_spoof_video_streams"),
                    ListPreference("revanced_spoof_video_streams_client_type"),
                    NonInteractivePreference(
                        // Requires a key and title but the actual text is chosen at runtime.
                        key = "revanced_spoof_video_streams_about",
                        summaryKey = null,
                        tag = "app.revanced.extension.youtube.settings.preference.SpoofStreamingDataSideEffectsPreference"
                    ),
                    SwitchPreference("revanced_spoof_video_streams_av1"),
                    SwitchPreference("revanced_spoof_streaming_data_stats_for_nerds"),
                )
            )
        )
    }
)
