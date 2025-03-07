package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.youtube.misc.settings.PreferenceScreen

/**
 * Video quality settings.  Used to organize all speed related settings together.
 */
internal val settingsMenuVideoQualityGroup = mutableSetOf<BasePreference>()

@Suppress("unused")
val videoQualityPatch = bytecodePatch(
    name = "Video quality",
    description = "Adds options to use the advanced video quality menu and set default video qualities."
) {
    dependsOn(
        rememberVideoQualityPatch,
        advancedVideoQualityMenuPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        )
    )

    execute {
        PreferenceScreen.VIDEO.addPreferences(
            // Keep the preferences organized together.
            PreferenceCategory(
                key = "revanced_01_video_key", // Dummy key to force the quality preferences first.
                titleKey = null,
                sorting = Sorting.UNSORTED,
                tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                preferences = settingsMenuVideoQualityGroup
            )
        )
    }
}
