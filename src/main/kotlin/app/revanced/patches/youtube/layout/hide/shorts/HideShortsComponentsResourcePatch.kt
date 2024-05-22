package app.revanced.patches.youtube.layout.hide.shorts

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal var reelMultipleItemShelfId = -1L
    private set
internal var reelPlayerRightCellButtonHeight = -1L
    private set

internal val hideShortsComponentsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.shorts.HideShortsComponentsResourcePatch")

        PreferenceScreen.SHORTS.addPreferences(
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

        reelPlayerRightCellButtonHeight = resourceMappings[
            "dimen",
            "reel_player_right_cell_button_height",
        ]

        // Resource not present in new versions of the app.
        try {
            resourceMappings[
                "dimen",
                "reel_player_right_cell_button_height",
            ]
        } catch (e: NoSuchElementException) {
            return@execute
        }.also { reelPlayerRightCellButtonHeight = it }
    }
}
