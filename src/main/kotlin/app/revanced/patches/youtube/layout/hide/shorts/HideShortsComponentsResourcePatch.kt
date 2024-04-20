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
            SwitchPreference("revanced_hide_shorts_home"),
            SwitchPreference("revanced_hide_shorts_subscriptions"),
            SwitchPreference("revanced_hide_shorts_search"),

            // Shorts player components.
            // Ideally each group should be ordered similar to how they appear in the UI
            // since this Setting menu currently uses the ordering used here.

            // Vertical row of buttons on right side of the screen.
            SwitchPreference("revanced_hide_shorts_like_button"),
            SwitchPreference("revanced_hide_shorts_dislike_button"),
            SwitchPreference("revanced_hide_shorts_comments_button"),
            SwitchPreference("revanced_hide_shorts_share_button"),
            SwitchPreference("revanced_hide_shorts_remix_button"),
            SwitchPreference("revanced_hide_shorts_sound_button"),

            // Everything else.
            SwitchPreference("revanced_hide_shorts_join_button"),
            SwitchPreference("revanced_hide_shorts_subscribe_button"),
            SwitchPreference("revanced_hide_shorts_paused_overlay_buttons"),
            SwitchPreference("revanced_hide_shorts_save_sound_button"),
            SwitchPreference("revanced_hide_shorts_shop_button"),
            SwitchPreference("revanced_hide_shorts_tagged_products"),
            SwitchPreference("revanced_hide_shorts_search_suggestions"),
            SwitchPreference("revanced_hide_shorts_location_label"),
            SwitchPreference("revanced_hide_shorts_channel_bar"),
            SwitchPreference("revanced_hide_shorts_info_panel"),
            SwitchPreference("revanced_hide_shorts_full_video_link_label"),
            SwitchPreference("revanced_hide_shorts_video_title"),
            SwitchPreference("revanced_hide_shorts_sound_metadata_label"),
            SwitchPreference("revanced_hide_shorts_navigation_bar"),
        )

        reelPlayerRightCellButtonHeight = ResourceMappingPatch[
            "dimen",
            "reel_player_right_cell_button_height",
        ]

        // Resource not present in new versions of the app.
        try {
            ResourceMappingPatch[
                "dimen",
                "reel_player_right_cell_button_height",
            ]
        } catch (e: NoSuchElementException) {
            return
        }.also { reelPlayerRightCellButtonHeight = it }
    }
}
