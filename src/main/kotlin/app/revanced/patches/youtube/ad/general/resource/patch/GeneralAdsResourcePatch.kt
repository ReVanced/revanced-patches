package app.revanced.patches.youtube.ad.general.resource.patch

import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.ad.general.annotation.GeneralAdsCompatibility
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch.PreferenceScreen

@DependsOn(
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class,
        ResourceMappingPatch::class
    ]
)
@GeneralAdsCompatibility
@Version("0.0.1")
class GeneralAdsResourcePatch : ResourcePatch {
    internal companion object {
        var adAttributionId: Long = -1
        var reelMultipleItemShelfId: Long = -1
    }

    override fun execute(context: ResourceContext): PatchResult {
        PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_adremover_separator",
                StringResource("revanced_adremover_separator_title", "Hide gray separator"),
                true,
                StringResource("revanced_adremover_separator_summary_on", "Gray separators are hidden"),
                StringResource("revanced_adremover_separator_summary_off", "Gray separators are shown")
            ),
            SwitchPreference(
                "revanced_adremover_hide_channel_guidelines",
                StringResource("revanced_adremover_hide_channel_guidelines_enabled_title", "Hide channel guidelines"),
                true,
                StringResource(
                    "revanced_adremover_hide_channel_guidelines_enabled_summary_on",
                    "Channel guidelines are hidden"
                ),
                StringResource(
                    "revanced_adremover_hide_channel_guidelines_enabled_summary_off",
                    "Channel guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_adremover_chapter_teaser",
                StringResource(
                    "revanced_adremover_chapter_teaser_enabled_title",
                    "Hide chapter teaser under videos"
                ),
                true,
                StringResource(
                    "revanced_adremover_chapter_teaser_enabled_summary_on",
                    "Chapter teasers are hidden"
                ),
                StringResource(
                    "revanced_adremover_chapter_teaser_enabled_summary_off",
                    "Chapter teasers are shown"
                )
            ),
            SwitchPreference(
                "revanced_adremover_merchandise",
                StringResource("revanced_adremover_merchandise_enabled_title", "Hide merchandise banners"),
                true,
                StringResource("revanced_adremover_merchandise_enabled_summary_on", "Merchandise banners are hidden"),
                StringResource("revanced_adremover_merchandise_enabled_summary_off", "Merchandise banners are shown")
            ),
            SwitchPreference(
                "revanced_adremover_community_posts_removal",
                StringResource("revanced_adremover_community_posts_enabled_title", "Hide community posts"),
                false,
                StringResource("revanced_adremover_community_posts_enabled_summary_on", "Community posts are hidden"),
                StringResource("revanced_adremover_community_posts_enabled_summary_off", "Community posts are shown")
            ),
            SwitchPreference(
                "revanced_adremover_compact_banner_removal",
                StringResource("revanced_adremover_compact_banner_enabled_title", "Hide compact banners"),
                true,
                StringResource("revanced_adremover_compact_banner_enabled_summary_on", "Compact banners are hidden"),
                StringResource("revanced_adremover_compact_banner_enabled_summary_off", "Compact banners are shown")
            ),
            SwitchPreference(
                "revanced_adremover_view_products",
                StringResource("revanced_adremover_view_products_title", "Hide banner to view products"),
                true,
                StringResource("revanced_adremover_view_products_summary_on", "Banner is hidden"),
                StringResource("revanced_adremover_view_products_summary_off", "Banner is shown")
            ),
            SwitchPreference(
                "revanced_adremover_web_search_result",
                StringResource("revanced_adremover_web_search_result_panel_title", "Hide web search results"),
                true,
                StringResource("revanced_adremover_web_search_result_summary_on", "Web search results are hidden"),
                StringResource("revanced_adremover_web_search_result_summary_off", "Web search results are shown")
            ),
            SwitchPreference(
                "revanced_adremover_movie",
                StringResource("revanced_adremover_movie_enabled_title", "Hide movies section"),
                true,
                StringResource("revanced_adremover_movie_enabled_summary_on", "Movies section is hidden"),
                StringResource("revanced_adremover_movie_enabled_summary_off", "Movies section is shown")
            ),
            SwitchPreference(
                "revanced_adremover_feed_survey",
                StringResource("revanced_adremover_feed_survey_enabled_title", "Hide feed surveys"),
                true,
                StringResource("revanced_adremover_feed_survey_enabled_summary_on", "Feed surveys are hidden"),
                StringResource("revanced_adremover_feed_survey_enabled_summary_off", "Feed surveys are shown")
            ),
            SwitchPreference(
                "revanced_adremover_shorts",
                StringResource("revanced_adremover_shorts_enabled_title", "Hide shorts"),
                true,
                StringResource("revanced_adremover_shorts_enabled_summary_on", "Shorts are hidden"),
                StringResource("revanced_adremover_shorts_enabled_summary_off", "Shorts are shown")
            ),
            SwitchPreference(
                "revanced_adremover_community_guidelines",
                StringResource("revanced_adremover_community_guidelines_enabled_title", "Hide community guidelines"),
                true,
                StringResource(
                    "revanced_adremover_community_guidelines_enabled_summary_on",
                    "Community guidelines are hidden"
                ),
                StringResource(
                    "revanced_adremover_community_guidelines_enabled_summary_off",
                    "Community guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_adremover_subscribers_community_guidelines_removal",
                StringResource(
                    "revanced_adremover_subscribers_community_guidelines_enabled_title",
                    "Hide subscribers community guidelines"
                ),
                true,
                StringResource(
                    "revanced_adremover_subscribers_community_guidelines_enabled_summary_on",
                    "Subscribers community guidelines are hidden"
                ),
                StringResource(
                    "revanced_adremover_subscribers_community_guidelines_enabled_summary_off",
                    "Subscribers community guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_adremover_channel_member_shelf_removal",
                StringResource("revanced_adremover_channel_member_shelf_enabled_title", "Hide channel member shelf"),
                true,
                StringResource(
                    "revanced_adremover_channel_member_shelf_enabled_summary_on",
                    "Channel member shelf is hidden"
                ),
                StringResource(
                    "revanced_adremover_channel_member_shelf_enabled_summary_off",
                    "Channel member shelf is shown"
                )
            ),
            SwitchPreference(
                "revanced_adremover_emergency_box_removal",
                StringResource("revanced_adremover_emergency_box_enabled_title", "Hide emergency boxes"),
                true,
                StringResource("revanced_adremover_emergency_box_enabled_summary_on", "Emergency boxes are hidden"),
                StringResource("revanced_adremover_emergency_box_enabled_summary_off", "Emergency boxes are shown")
            ),
            SwitchPreference(
                "revanced_adremover_info_panel",
                StringResource("revanced_adremover_info_panel_enabled_title", "Hide info panels"),
                true,
                StringResource("revanced_adremover_info_panel_enabled_summary_on", "Info panels are hidden"),
                StringResource("revanced_adremover_info_panel_enabled_summary_off", "Info panels are shown")
            ),
            SwitchPreference(
                "revanced_adremover_medical_panel",
                StringResource("revanced_adremover_medical_panel_enabled_title", "Hide medical panels"),
                true,
                StringResource("revanced_adremover_medical_panel_enabled_summary_on", "Medical panels are hidden"),
                StringResource("revanced_adremover_medical_panel_enabled_summary_off", "Medical panels are shown")
            ),
            SwitchPreference(
                "revanced_hide_channel_bar",
                StringResource("revanced_hide_channel_bar_title", "Hide channel bar"),
                false,
                StringResource("revanced_hide_channel_bar_summary_on", "Channel bar is hidden"),
                StringResource("revanced_hide_channel_bar_summary_off", "Channel bar is shown")
            ),
            SwitchPreference(
                "revanced_hide_quick_actions",
                StringResource("revanced_hide_quick_actions_title", "Hide quick actions in fullscreen"),
                false,
                StringResource("revanced_hide_quick_actions_summary_on", "Quick actions are hidden"),
                StringResource("revanced_hide_quick_actions_summary_off", "Quick actions are shown")
            ),
            SwitchPreference(
                "revanced_hide_related_videos",
                StringResource("revanced_hide_related_videos_title", "Hide related videos in quick actions"),
                false,
                StringResource("revanced_hide_related_videos_summary_on", "Related videos are hidden"),
                StringResource("revanced_hide_related_videos_summary_off", "Related videos are shown")
            ),
            SwitchPreference(
                "revanced_hide_image_shelf",
                StringResource("revanced_hide_image_shelf", "Hide image shelf in search results"),
                true,
                StringResource("revanced_hide_image_shelf_summary_on", "Image shelf is hidden"),
                StringResource("revanced_hide_image_shelf_summary_off", "Image shelf is shown")
            ),
            SwitchPreference(
                "revanced_hide_audio_track_button",
                StringResource("revanced_hide_audio_track_button_title", "Hide audio track button"),
                false,
                StringResource("revanced_hide_audio_track_button_on", "Audio track button is hidden"),
                StringResource("revanced_hide_audio_track_button_off", "Audio track button is shown")
            )
        )

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_adremover_ad_removal",
                StringResource("revanced_adremover_ad_removal_enabled_title", "Hide general ads"),
                true,
                StringResource("revanced_adremover_ad_removal_enabled_summary_on", "General ads are hidden"),
                StringResource("revanced_adremover_ad_removal_enabled_summary_off", "General ads are shown")
            ),
            SwitchPreference(
                "revanced_adremover_buttoned",
                StringResource("revanced_adremover_buttoned_enabled_title", "Hide buttoned ad"),
                true,
                StringResource("revanced_adremover_buttoned_enabled_summary_on", "Buttoned ads are hidden"),
                StringResource("revanced_adremover_buttoned_enabled_summary_off", "Buttoned ads are shown")
            ),
            SwitchPreference(
                "revanced_adremover_paid_content",
                StringResource("revanced_adremover_paid_content_enabled_title", "Hide paid content"),
                true,
                StringResource("revanced_adremover_paid_content_enabled_summary_on", "Paid content is hidden"),
                StringResource("revanced_adremover_paid_content_enabled_summary_off", "Paid content is shown")
            ),
            SwitchPreference(
                "revanced_adremover_hide_latest_posts",
                StringResource("revanced_adremover_hide_latest_posts_enabled_title", "Hide latest posts"),
                true,
                StringResource("revanced_adremover_hide_latest_posts_enabled_summary_on", "Latest posts are hidden"),
                StringResource("revanced_adremover_hide_latest_posts_enabled_summary_off", "Latest posts are shown")
            ),
            SwitchPreference(
                "revanced_adremover_self_sponsor",
                StringResource("revanced_adremover_self_sponsor_enabled_title", "Hide self sponsored cards"),
                true,
                StringResource("revanced_adremover_self_sponsor_enabled_summary_on", "Self sponsored cards are hidden"),
                StringResource("revanced_adremover_self_sponsor_enabled_summary_off", "Self sponsored cards are shown")
            ),
            PreferenceScreen(
                "revanced_adremover_custom",
                StringResource("revanced_adremover_custom_title", "Custom filter"),
                listOf(
                    SwitchPreference(
                        "revanced_adremover_custom_enabled",
                        StringResource(
                            "revanced_adremover_custom_enabled_title",
                            "Enable custom filter"
                        ),
                        false,
                        StringResource(
                            "revanced_adremover_custom_enabled_summary_on",
                            "Custom filter is enabled"
                        ),
                        StringResource(
                            "revanced_adremover_custom_enabled_summary_off",
                            "Custom filter is disabled"
                        )
                    ),
                    // TODO: This should be a dynamic ListPreference, which does not exist yet
                    TextPreference(
                        "revanced_adremover_custom_strings",
                        StringResource("revanced_adremover_custom_strings_title", "Custom filter"),
                        InputType.STRING,
                        "",
                        StringResource(
                            "revanced_adremover_custom_strings_summary",
                            "Filter components by their name separated by a comma"
                        )
                    )
                )
            )
        )

        fun String.getId() = ResourceMappingPatch.resourceMappings.single { it.name == this }.id

        adAttributionId = "ad_attribution".getId()
        reelMultipleItemShelfId = "reel_multiple_items_shelf".getId()

        return PatchResultSuccess()
    }
}
