package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val openVideosFullscreenPatch = bytecodePatch(
    name = "Open videos fullscreen",
    description = "Adds an option to open videos in full screen portrait mode.",
) {
    dependsOn(
        openVideosFullscreenHookPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    execute {
        addResources("youtube", "layout.player.fullscreen.openVideosFullscreen")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_open_videos_fullscreen_portrait")
        )

        // Enable the logic for the user Setting to open regular videos fullscreen.
        openVideosFullscreenHookPatchExtensionFingerprint.method.returnEarly(true)
    }
}