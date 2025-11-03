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
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
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
                    SwitchPreference("revanced_hide_player_flyout_stable_volume"),
                    SwitchPreference("revanced_hide_player_flyout_listen_with_youtube_music"),
                    SwitchPreference("revanced_hide_player_flyout_help"),
                    SwitchPreference("revanced_hide_player_flyout_speed"),
                    SwitchPreference("revanced_hide_player_flyout_lock_screen"),
                    SwitchPreference(
                        key = "revanced_hide_player_flyout_audio_track",
                        tag = "app.revanced.extension.youtube.settings.preference.HideAudioFlyoutMenuPreference"
                    ),
                    SwitchPreference("revanced_hide_player_flyout_watch_in_vr"),
                    SwitchPreference("revanced_hide_player_flyout_sleep_timer"),
                    SwitchPreference("revanced_hide_player_flyout_video_quality"),
                    SwitchPreference("revanced_hide_player_flyout_video_quality_footer"),
                ),
            ),
        )

        addLithoFilter(filterClassDescriptor)
    }
}
