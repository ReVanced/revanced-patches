package app.revanced.patches.youtube.video.speed

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.video.speed.button.playbackSpeedButtonPatch
import app.revanced.patches.youtube.video.speed.custom.customPlaybackSpeedPatch
import app.revanced.patches.youtube.video.speed.remember.rememberPlaybackSpeedPatch

/**
 * Speed menu settings.
 */
internal val settingsMenuVideoSpeedGroup = mutableSetOf<BasePreference>()

@Suppress("unused")
val playbackSpeedPatch = bytecodePatch(
    name = "Playback speed",
    description = "Adds options to customize available playback speeds, remember the last playback speed selected " +
        "and show a speed dialog button in the video player.",
) {
    dependsOn(
        playbackSpeedButtonPatch,
        customPlaybackSpeedPatch,
        rememberPlaybackSpeedPatch,
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

    finalize {
        // Keep the preferences organized together.
        PreferenceScreen.VIDEO.addPreferences(
            PreferenceCategory(
                key = null,
                // The title does not show, but is used for sorting the group.
                titleKey = "revanced_custom_speed_menu_title",
                sorting = Sorting.UNSORTED,
                tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                preferences = settingsMenuVideoSpeedGroup
            )
        )
    }
}
