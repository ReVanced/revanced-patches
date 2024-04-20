package app.revanced.patches.youtube.layout.hide.player.flyoutmenupanel

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Player flyout menu",
    description = "Adds options to hide menu items that appear when pressing the gear icon in the video player.",
    dependencies = [
        LithoFilterPatch::class,
        PlayerTypeHookPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class
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
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object HidePlayerFlyoutMenuPatch : ResourcePatch() {

    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/PlayerFlyoutMenuItemsFilter;"

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
                PreferenceScreen(
                        key = "revanced_hide_player_flyout",
                        preferences = setOf(
                                SwitchPreference("revanced_hide_player_flyout_captions"),
                                SwitchPreference("revanced_hide_player_flyout_additional_settings"),
                                SwitchPreference("revanced_hide_player_flyout_loop_video"),
                                SwitchPreference("revanced_hide_player_flyout_ambient_mode"),
                                SwitchPreference("revanced_hide_player_flyout_report"),
                                SwitchPreference("revanced_hide_player_flyout_help"),
                                SwitchPreference("revanced_hide_player_flyout_speed"),
                                SwitchPreference("revanced_hide_player_flyout_lock_screen"),
                                SwitchPreference("revanced_hide_player_flyout_more_info"),
                                SwitchPreference("revanced_hide_player_flyout_audio_track"),
                                SwitchPreference("revanced_hide_player_flyout_watch_in_vr"),
                        )
                )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
