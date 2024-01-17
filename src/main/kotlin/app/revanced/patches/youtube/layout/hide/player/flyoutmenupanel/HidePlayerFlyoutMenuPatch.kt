package app.revanced.patches.youtube.layout.hide.player.flyoutmenupanel

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Player flyout menu",
    description = "Adds options to hide menu items that appear when pressing the gear icon in the video player.",
    dependencies = [
        LithoFilterPatch::class,
        PlayerTypeHookPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.34"
            ]
        )
    ]
)
@Suppress("unused")
object HidePlayerFlyoutMenuPatch : ResourcePatch() {
    private const val KEY = "revanced_hide_player_flyout"

    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/PlayerFlyoutMenuItemsFilter;"

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                KEY,
                StringResource("${KEY}_title", "Player flyout menu items"),
                listOf(
                    SwitchPreference(
                        "${KEY}_captions",
                        StringResource("${KEY}_captions_title", "Hide Captions menu"),
                        StringResource("${KEY}_captions_on", "Captions menu item is hidden"),
                        StringResource("${KEY}_captions_off", "Captions menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_additional_settings",
                        StringResource("${KEY}_additional_settings_title", "Hide Additional settings menu"),
                        StringResource("${KEY}_additional_settings_on", "Additional settings menu item is hidden"),
                        StringResource("${KEY}_additional_settings_off", "Additional settings menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_loop_video",
                        StringResource("${KEY}_loop_video_title", "Hide Loop video menu"),
                        StringResource("${KEY}_loop_video_on", "Loop video menu item is hidden"),
                        StringResource("${KEY}_loop_video_off", "Loop video menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_ambient_mode",
                        StringResource("${KEY}_ambient_mode_title", "Hide Ambient mode menu"),
                        StringResource("${KEY}_ambient_mode_on", "Ambient mode menu item is hidden"),
                        StringResource("${KEY}_ambient_mode_off", "Ambient mode menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_report",
                        StringResource("${KEY}_report_title", "Hide Report menu"),
                        StringResource("${KEY}_report_on", "Report menu item is hidden"),
                        StringResource("${KEY}_report_off", "Report menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_help",
                        StringResource("${KEY}_help_title", "Hide Help menu"),
                        StringResource("${KEY}_help_on", "Help menu item is hidden"),
                        StringResource("${KEY}_help_off", "Help menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_speed",
                        StringResource("${KEY}_speed_title", "Hide Speed menu"),
                        StringResource("${KEY}_speed_on", "Speed menu item is hidden"),
                        StringResource("${KEY}_speed_off", "Speed menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_more_info",
                        StringResource("${KEY}_more_info_title", "Hide More info menu"),
                        StringResource("${KEY}_more_info_on", "More info menu item is hidden"),
                        StringResource("${KEY}_more_info_off", "More info menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_audio_track",
                        StringResource("${KEY}_audio_track_title", "Hide Audio track menu"),
                        StringResource("${KEY}_audio_track_on", "Audio track menu item is hidden"),
                        StringResource("${KEY}_audio_track_off", "Audio track menu item is shown")
                    ),
                    SwitchPreference(
                        "${KEY}_watch_in_vr",
                        StringResource("${KEY}_watch_in_vr_title", "Hide Watch in VR menu"),
                        StringResource("${KEY}_watch_in_vr_on", "Watch in VR menu item is hidden"),
                        StringResource("${KEY}_watch_in_vr_off", "Watch in VR menu item is shown")
                    ),
                ),
                StringResource("${KEY}_summary", "Manage the visibility of player flyout menu items")
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
