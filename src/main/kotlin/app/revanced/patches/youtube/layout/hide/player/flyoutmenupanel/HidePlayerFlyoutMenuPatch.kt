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
                listOf(
                    SwitchPreference("revanced_hide_player_flyout_captions"),
                    SwitchPreference("revanced_hide_player_flyout_additional_settings"),
                    SwitchPreference("revanced_hide_player_flyout_loop_video"),
                    SwitchPreference("revanced_hide_player_flyout_ambient_mode"),
                    SwitchPreference("revanced_hide_player_flyout_report"),
                    SwitchPreference("revanced_hide_player_flyout_help"),
                    SwitchPreference("revanced_hide_player_flyout_speed"),
                    SwitchPreference("revanced_hide_player_flyout_more_info"),
                    SwitchPreference("revanced_hide_player_flyout_audio_track"),
                    SwitchPreference("revanced_hide_player_flyout_watch_in_vr")
                ),
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
