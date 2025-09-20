package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
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
        versionCheckPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        if (!is_19_46_or_greater) {
            throw PatchException("'Open videos fullscreen' requires 19.46.42 or greater")
        }

        addResources("youtube", "layout.player.fullscreen.openVideosFullscreen")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_open_videos_fullscreen_portrait")
        )

        // Enable the logic for the user Setting to open regular videos fullscreen.
        openVideosFullscreenHookPatchExtensionFingerprint.method.returnEarly(true)
    }
}