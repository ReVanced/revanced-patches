package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class])
object HideShortsComponentsResourcePatch : ResourcePatch() {
    internal var reelMultipleItemShelfId = -1L
    internal var reelPlayerRightCellButtonHeight = -1L

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("HideShortsComponents")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_shorts_preference_screen",
                "revanced_shorts_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_hide_shorts",
                        "revanced_hide_shorts_title",
                        "revanced_hide_shorts_on",
                        "revanced_hide_shorts_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_join_button",
                        "revanced_hide_shorts_join_button_title",
                        "revanced_hide_shorts_join_button_on",
                        "revanced_hide_shorts_join_button_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_subscribe_button",
                        "revanced_hide_shorts_subscribe_button_title",
                        "revanced_hide_shorts_subscribe_button_on",
                        "revanced_hide_shorts_subscribe_button_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_subscribe_button_paused",
                        "revanced_hide_shorts_subscribe_button_paused_title",
                        "revanced_hide_shorts_subscribe_button_paused_on",
                        "revanced_hide_shorts_subscribe_button_paused_off",
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_thanks_button",
                        "revanced_hide_shorts_thanks_button_title",
                        "revanced_hide_shorts_thanks_button_on",
                        "revanced_hide_shorts_thanks_button_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_comments_button",
                        "revanced_hide_shorts_comments_button_title",
                        "revanced_hide_shorts_comments_button_on",
                        "revanced_hide_shorts_comments_button_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_remix_button",
                        "revanced_hide_shorts_remix_button_title",
                        "revanced_hide_shorts_remix_button_on",
                        "revanced_hide_shorts_remix_button_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_share_button",
                        "revanced_hide_shorts_share_button_title",
                        "revanced_hide_shorts_share_button_on",
                        "revanced_hide_shorts_share_button_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_info_panel",
                        "revanced_hide_shorts_info_panel_title",
                        "revanced_hide_shorts_info_panel_on",
                        "revanced_hide_shorts_info_panel_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_channel_bar",
                        "revanced_hide_shorts_channel_bar_title",
                        "revanced_hide_shorts_channel_bar_on",
                        "revanced_hide_shorts_channel_bar_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_sound_button",
                        "revanced_hide_shorts_sound_button_title",
                        "revanced_hide_shorts_sound_button_on",
                        "revanced_hide_shorts_sound_button_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_navigation_bar",
                        "revanced_hide_shorts_navigation_bar_title",
                        "revanced_hide_shorts_navigation_bar_on",
                        "revanced_hide_shorts_navigation_bar_off"
                    )
                ),
                "revanced_shorts_preference_screen_summary"
            )
        )

        fun String.getId() = ResourceMappingPatch.resourceMappings.single { it.name == this }.id

        reelMultipleItemShelfId = "reel_multiple_items_shelf".getId()
        reelPlayerRightCellButtonHeight = "reel_player_right_cell_button_height".getId()
    }
}