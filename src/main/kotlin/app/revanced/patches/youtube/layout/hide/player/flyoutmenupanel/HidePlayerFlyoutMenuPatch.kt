package app.revanced.patches.youtube.layout.hide.player.flyoutmenupanel

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch

@Patch(
    name = "Player flyout menu",
    description = "Hides player flyout menu items.",
    dependencies = [
        LithoFilterPatch::class,
        PlayerTypeHookPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube", [
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.41",
            "18.45.43"
        ])
    ]
)
@Suppress("unused")
object HidePlayerFlyoutMenuPatch : ResourcePatch() {
    private const val KEY = "revanced_hide_player_flyout"

    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/components/PlayerFlyoutMenuItemsFilter;"

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("HidePlayerFlyoutMenu")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_hide_player_flyout_screen",
                "revanced_hide_player_flyout_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_hide_player_flyout_captions",
                        "revanced_hide_player_flyout_captions_title",
                        "revanced_hide_player_flyout_captions_on",
                        "revanced_hide_player_flyout_captions_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_additional_settings",
                        "revanced_hide_player_flyout_additional_settings_title",
                        "revanced_hide_player_flyout_additional_settings_on",
                        "revanced_hide_player_flyout_additional_settings_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_loop_video",
                        "revanced_hide_player_flyout_loop_video_title",
                        "revanced_hide_player_flyout_loop_video_on",
                        "revanced_hide_player_flyout_loop_video_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_ambient_mode",
                        "revanced_hide_player_flyout_ambient_mode_title",
                        "revanced_hide_player_flyout_ambient_mode_on",
                        "revanced_hide_player_flyout_ambient_mode_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_report",
                        "revanced_hide_player_flyout_report_title",
                        "revanced_hide_player_flyout_report_on",
                        "revanced_hide_player_flyout_report_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_help",
                        "revanced_hide_player_flyout_help_title",
                        "revanced_hide_player_flyout_help_on",
                        "revanced_hide_player_flyout_help_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_speed",
                        "revanced_hide_player_flyout_speed_title",
                        "revanced_hide_player_flyout_speed_on",
                        "revanced_hide_player_flyout_speed_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_more_info",
                        "revanced_hide_player_flyout_more_info_title",
                        "revanced_hide_player_flyout_more_info_on",
                        "revanced_hide_player_flyout_more_info_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_audio_track",
                        "revanced_hide_player_flyout_audio_track_title",
                        "revanced_hide_player_flyout_audio_track_on",
                        "revanced_hide_player_flyout_audio_track_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_player_flyout_watch_in_vr",
                        "revanced_hide_player_flyout_watch_in_vr_title",
                        "revanced_hide_player_flyout_watch_in_vr_on",
                        "revanced_hide_player_flyout_watch_in_vr_off",
                    ),
                ),
                "revanced_hide_player_flyout_screen_summary",
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
