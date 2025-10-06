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
    description = "Adds options to set default video qualities and always use the advanced video quality menu."
) {
    dependsOn(
        rememberVideoQualityPatch,
        advancedVideoQualityMenuPatch,
        videoQualityDialogButtonPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
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
