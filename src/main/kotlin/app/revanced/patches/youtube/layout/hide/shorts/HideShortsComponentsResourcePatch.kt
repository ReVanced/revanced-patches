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
                listOf(
                    SwitchPreference("revanced_hide_shorts"),
                    SwitchPreference("revanced_hide_shorts_join_button"),
                    SwitchPreference("revanced_hide_shorts_subscribe_button"),
                    SwitchPreference("revanced_hide_shorts_subscribe_button_paused"),
                    SwitchPreference("revanced_hide_shorts_thanks_button"),
                    SwitchPreference("revanced_hide_shorts_comments_button"),
                    SwitchPreference("revanced_hide_shorts_remix_button"),
                    SwitchPreference("revanced_hide_shorts_share_button"),
                    SwitchPreference("revanced_hide_shorts_info_panel"),
                    SwitchPreference("revanced_hide_shorts_channel_bar"),
                    SwitchPreference("revanced_hide_shorts_sound_button"),
                    SwitchPreference("revanced_hide_shorts_navigation_bar")
                )
            )
        )

        fun String.getId() = ResourceMappingPatch.resourceMappings.single { it.name == this }.id

        reelMultipleItemShelfId = "reel_multiple_items_shelf".getId()
        reelPlayerRightCellButtonHeight = "reel_player_right_cell_button_height".getId()
    }
}