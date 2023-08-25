package app.revanced.patches.youtube.layout.hide.shorts.resource.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
class HideShortsComponentsResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_shorts_preference_screen",
                StringResource("revanced_shorts_preference_screen_title", "Shorts components"),
                listOf(
                    SwitchPreference(
                        "revanced_hide_shorts",
                        StringResource("revanced_hide_shorts_title", "Hide Shorts in feed"),
                        StringResource("revanced_hide_shorts_on", "Shorts are hidden"),
                        StringResource("revanced_hide_shorts_off", "Shorts are shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_join_button",
                        StringResource("revanced_hide_shorts_join_button_title", "Hide join button"),
                        StringResource("revanced_hide_shorts_join_button_on", "Join button is hidden"),
                        StringResource("revanced_hide_shorts_join_button_off", "Join button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_subscribe_button",
                        StringResource("revanced_hide_shorts_subscribe_button_title", "Hide subscribe button"),
                        StringResource("revanced_hide_shorts_subscribe_button_on", "Subscribe button is hidden"),
                        StringResource("revanced_hide_shorts_subscribe_button_off", "Subscribe button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_thanks_button",
                        StringResource("revanced_hide_shorts_thanks_button_title", "Hide thanks button"),
                        StringResource("revanced_hide_shorts_thanks_button_on", "Thanks button is hidden"),
                        StringResource("revanced_hide_shorts_thanks_button_off", "Thanks button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_comments_button",
                        StringResource("revanced_hide_shorts_comments_button_title", "Hide comments button"),
                        StringResource("revanced_hide_shorts_comments_button_on", "Comments button is hidden"),
                        StringResource("revanced_hide_shorts_comments_button_off", "Comments button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_remix_button",
                        StringResource("revanced_hide_shorts_remix_button_title", "Hide remix button"),
                        StringResource("revanced_hide_shorts_remix_button_on", "Remix button is hidden"),
                        StringResource("revanced_hide_shorts_remix_button_off", "Remix button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_share_button",
                        StringResource("revanced_hide_shorts_share_button_title", "Hide share button"),
                        StringResource("revanced_hide_shorts_share_button_on", "Share button is hidden"),
                        StringResource("revanced_hide_shorts_share_button_off", "Share button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_info_panel",
                        StringResource("revanced_hide_shorts_info_panel_title", "Hide info panel"),
                        StringResource("revanced_hide_shorts_info_panel_on", "Info panel is hidden"),
                        StringResource("revanced_hide_shorts_info_panel_off", "Info panel is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_channel_bar",
                        StringResource("revanced_hide_shorts_channel_bar_title", "Hide channel bar"),
                        StringResource("revanced_hide_shorts_channel_bar_on", "Channel bar is hidden"),
                        StringResource("revanced_hide_shorts_channel_bar_off", "Channel bar is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_sound_button",
                        StringResource("revanced_hide_shorts_sound_button_title", "Hide sound button"),
                        StringResource("revanced_hide_shorts_sound_button_on", "Sound button is hidden"),
                        StringResource("revanced_hide_shorts_sound_button_off", "Sound button is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_navigation_bar",
                        StringResource("revanced_hide_shorts_navigation_bar_title", "Hide navigation bar"),
                        StringResource("revanced_hide_shorts_navigation_bar_on", "Navigation bar is hidden"),
                        StringResource("revanced_hide_shorts_navigation_bar_off", "Navigation bar is shown")
                    )
                ),
                StringResource("revanced_shorts_preference_screen_summary", "Manage the visibility of Shorts components")
            )
        )

        fun String.getId() = ResourceMappingPatch.resourceMappings.single { it.name == this }.id

        reelMultipleItemShelfId = "reel_multiple_items_shelf".getId()
        reelPlayerRightCellButtonHeight = "reel_player_right_cell_button_height".getId()
    }

    companion object {
        var reelMultipleItemShelfId = -1L
        var reelPlayerRightCellButtonHeight = -1L
    }
}