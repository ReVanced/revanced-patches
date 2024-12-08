package app.revanced.patches.youtube.misc.spoof

import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.spoof.spoofVideoStreamsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

val spoofVideoStreamsPatch = spoofVideoStreamsPatch({
    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
        ),
    )

    dependsOn(
        userAgentClientSpoofPatch,
        settingsPatch,
    )
}, {
    addResources("youtube", "misc.fix.playback.spoofVideoStreamsPatch")

    PreferenceScreen.MISC.addPreferences(
        PreferenceScreenPreference(
            key = "revanced_spoof_video_streams_screen",
            sorting = PreferenceScreenPreference.Sorting.UNSORTED,
            preferences = setOf(
                SwitchPreference("revanced_spoof_video_streams"),
                ListPreference(
                    "revanced_spoof_video_streams_client",
                    summaryKey = null,
                ),
                SwitchPreference(
                    "revanced_spoof_video_streams_ios_force_avc",
                    tag = "app.revanced.extension.youtube.settings.preference.ForceAVCSpoofingPreference",
                ),
                NonInteractivePreference("revanced_spoof_video_streams_about_android_vr"),
                NonInteractivePreference("revanced_spoof_video_streams_about_ios"),
            ),
        ),
    )
})
