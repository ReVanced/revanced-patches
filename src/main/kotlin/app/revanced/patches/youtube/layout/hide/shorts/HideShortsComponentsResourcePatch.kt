package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class, AddResourcesPatch::class])
object HideShortsComponentsResourcePatch : ResourcePatch() {
    internal var reelMultipleItemShelfId = -1L
    internal var reelPlayerRightCellButtonHeight = -1L

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SHORTS.addPreferences(
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

        ResourceMappingPatch.resourceMappings.find {
            it.type == "layout" && it.name == "reel_multiple_items_shelf"
        }?.also {
            reelMultipleItemShelfId = it.id
        }

        reelPlayerRightCellButtonHeight =
            ResourceMappingPatch.resourceMappings.single {
                it.type == "dimen" && it.name == "reel_player_right_cell_button_height"
            }.id
    }
}