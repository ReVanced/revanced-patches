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
 * Speed menu settings.  Used to organize all speed related settings together.
 */
internal val settingsMenuVideoSpeedGroup = mutableListOf<BasePreference>()

@Suppress("unused")
val playbackSpeedPatch = bytecodePatch(
    name = "Playback speed",
    description = "Adds options to customize available playback speeds, set default a playback speed, " +
        "and show a speed dialog button in the video player.",
) {
    dependsOn(
        rememberPlaybackSpeedPatch,
        customPlaybackSpeedPatch,
        playbackSpeedButtonPatch,
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
            PreferenceCategory(
                key = "revanced_zz_video_key", // Dummy key to force the speed settings last.
                titleKey = null,
                sorting = Sorting.UNSORTED,
                tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                preferences = settingsMenuVideoSpeedGroup.toSet()
            )
        )
    }
}
