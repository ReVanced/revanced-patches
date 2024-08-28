package app.revanced.patches.youtube.layout.hide.player.flyoutmenupanel

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

@Suppress("unused")
val hidePlayerFlyoutMenuPatch = bytecodePatch(
    name = "Hide player flyout menu items",
    description = "Adds options to hide menu items that appear when pressing the gear icon in the video player.",
) {
    dependsOn(
        lithoFilterPatch,
        playerTypeHookPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    execute {
        val filterClassDescriptor = "Lapp/revanced/extension/youtube/patches/components/PlayerFlyoutMenuItemsFilter;"

        addResources("youtube", "layout.hide.player.flyoutmenupanel.hidePlayerFlyoutMenuPatch")

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_hide_player_flyout",
                preferences = setOf(
                    SwitchPreference("revanced_hide_player_flyout_captions"),
                    SwitchPreference("revanced_hide_player_flyout_additional_settings"),
                    SwitchPreference("revanced_hide_player_flyout_loop_video"),
                    SwitchPreference("revanced_hide_player_flyout_ambient_mode"),
                    SwitchPreference("revanced_hide_player_flyout_help"),
                    SwitchPreference("revanced_hide_player_flyout_speed"),
                    SwitchPreference("revanced_hide_player_flyout_lock_screen"),
                    SwitchPreference("revanced_hide_player_flyout_more_info"),
                    SwitchPreference("revanced_hide_player_flyout_audio_track"),
                    SwitchPreference("revanced_hide_player_flyout_watch_in_vr"),
                    SwitchPreference("revanced_hide_video_quality_menu_footer"),
                ),
            ),
        )

        addLithoFilter(filterClassDescriptor)
    }
}
