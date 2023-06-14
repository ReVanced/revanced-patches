package app.revanced.patches.youtube.ad.general.resource.patch

import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.ad.general.annotation.HideAdsCompatibility
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
@HideAdsCompatibility
@Version("0.0.1")
class HideAdsResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_gray_separator",
                StringResource("revanced_hide_gray_separator_title", "Hide gray separator"),
                StringResource("revanced_hide_gray_separator_summary_on", "Gray separators are hidden"),
                StringResource("revanced_hide_gray_separator_summary_off", "Gray separators are shown")
            ),
            SwitchPreference(
                "revanced_hide_channel_guidelines",
                StringResource("revanced_hide_channel_guidelines_title", "Hide channel guidelines"),
                StringResource(
                    "revanced_hide_channel_guidelines_summary_on",
                    "Channel guidelines are hidden"
                ),
                StringResource(
                    "revanced_hide_channel_guidelines_summary_off",
                    "Channel guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_chapter_teaser",
                StringResource(
                    "revanced_hide_chapter_teaser_title",
                    "Hide chapter teaser under videos"
                ),
                StringResource(
                    "revanced_hide_chapter_teaser_summary_on",
                    "Chapter teasers are hidden"
                ),
                StringResource(
                    "revanced_hide_chapter_teaser_summary_off",
                    "Chapter teasers are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_merchandise_banners",
                StringResource("revanced_hide_merchandise_banners_title", "Hide merchandise banners"),
                StringResource("revanced_hide_merchandise_banners_summary_on", "Merchandise banners are hidden"),
                StringResource("revanced_hide_merchandise_banners_summary_off", "Merchandise banners are shown")
            ),
            SwitchPreference(
                "revanced_hide_community_posts",
                StringResource("revanced_hide_community_posts_title", "Hide community posts"),
                StringResource("revanced_hide_community_posts_summary_on", "Community posts are hidden"),
                StringResource("revanced_hide_community_posts_summary_off", "Community posts are shown")
            ),
            SwitchPreference(
                "revanced_hide_compact_banner",
                StringResource("revanced_hide_compact_banner_title", "Hide compact banners"),
                StringResource("revanced_hide_compact_banner_summary_on", "Compact banners are hidden"),
                StringResource("revanced_hide_compact_banner_summary_off", "Compact banners are shown")
            ),
            SwitchPreference(
                "revanced_hide_products_banner",
                StringResource("revanced_hide_products_banner_title", "Hide banner to view products"),
                StringResource("revanced_hide_products_banner_summary_on", "Banner is hidden"),
                StringResource("revanced_hide_products_banner_summary_off", "Banner is shown")
            ),
            SwitchPreference(
                "revanced_hide_web_search_results",
                StringResource("revanced_hide_web_search_results_title", "Hide web search results"),
                StringResource("revanced_hide_web_search_results_summary_on", "Web search results are hidden"),
                StringResource("revanced_hide_web_search_results_summary_off", "Web search results are shown")
            ),
            SwitchPreference(
                "revanced_hide_movies_section",
                StringResource("revanced_hide_movies_section_title", "Hide movies section"),
                StringResource("revanced_hide_movies_section_summary_on", "Movies section is hidden"),
                StringResource("revanced_hide_movies_section_summary_off", "Movies section is shown")
            ),
            SwitchPreference(
                "revanced_hide_feed_survey",
                StringResource("revanced_hide_feed_survey_title", "Hide feed surveys"),
                StringResource("revanced_hide_feed_survey_summary_on", "Feed surveys are hidden"),
                StringResource("revanced_hide_feed_survey_summary_off", "Feed surveys are shown")
            ),
            SwitchPreference(
                "revanced_hide_community_guidelines",
                StringResource("revanced_hide_community_guidelines_title", "Hide community guidelines"),
                StringResource(
                    "revanced_hide_community_guidelines_summary_on",
                    "Community guidelines are hidden"
                ),
                StringResource(
                    "revanced_hide_community_guidelines_summary_off",
                    "Community guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_subscribers_community_guidelines",
                StringResource(
                    "revanced_hide_subscribers_community_guidelines_title",
                    "Hide subscribers community guidelines"
                ),
                StringResource(
                    "revanced_hide_subscribers_community_guidelines_summary_on",
                    "Subscribers community guidelines are hidden"
                ),
                StringResource(
                    "revanced_hide_subscribers_community_guidelines_summary_off",
                    "Subscribers community guidelines are shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_channel_member_shelf",
                StringResource("revanced_hide_channel_member_shelf_title", "Hide channel member shelf"),
                StringResource(
                    "revanced_hide_channel_member_shelf_summary_on",
                    "Channel member shelf is hidden"
                ),
                StringResource(
                    "revanced_hide_channel_member_shelf_summary_off",
                    "Channel member shelf is shown"
                )
            ),
            SwitchPreference(
                "revanced_hide_emergency_box",
                StringResource("revanced_hide_emergency_box_title", "Hide emergency boxes"),
                StringResource("revanced_hide_emergency_box_summary_on", "Emergency boxes are hidden"),
                StringResource("revanced_hide_emergency_box_summary_off", "Emergency boxes are shown")
            ),
            SwitchPreference(
                "revanced_hide_info_panels",
                StringResource("revanced_hide_info_panels_title", "Hide info panels"),
                StringResource("revanced_hide_info_panels_summary_on", "Info panels are hidden"),
                StringResource("revanced_hide_info_panels_summary_off", "Info panels are shown")
            ),
            SwitchPreference(
                "revanced_hide_medical_panels",
                StringResource("revanced_hide_medical_panels_title", "Hide medical panels"),
                StringResource("revanced_hide_medical_panels_summary_on", "Medical panels are hidden"),
                StringResource("revanced_hide_medical_panels_summary_off", "Medical panels are shown")
            ),
            SwitchPreference(
                "revanced_hide_channel_bar",
                StringResource("revanced_hide_channel_bar_title", "Hide channel bar"),
                StringResource("revanced_hide_channel_bar_summary_on", "Channel bar is hidden"),
                StringResource("revanced_hide_channel_bar_summary_off", "Channel bar is shown")
            ),
            SwitchPreference(
                "revanced_hide_quick_actions",
                StringResource("revanced_hide_quick_actions_title", "Hide quick actions in fullscreen"),
                StringResource("revanced_hide_quick_actions_summary_on", "Quick actions are hidden"),
                StringResource("revanced_hide_quick_actions_summary_off", "Quick actions are shown")
            ),
            SwitchPreference(
                "revanced_hide_related_videos",
                StringResource("revanced_hide_related_videos_title", "Hide related videos in quick actions"),
                StringResource("revanced_hide_related_videos_summary_on", "Related videos are hidden"),
                StringResource("revanced_hide_related_videos_summary_off", "Related videos are shown")
            ),
            SwitchPreference(
                "revanced_hide_image_shelf",
                StringResource("revanced_hide_image_shelf", "Hide image shelf in search results"),
                StringResource("revanced_hide_image_shelf_summary_on", "Image shelf is hidden"),
                StringResource("revanced_hide_image_shelf_summary_off", "Image shelf is shown")
            ),
            SwitchPreference(
                "revanced_hide_audio_track_button",
                StringResource("revanced_hide_audio_track_button_title", "Hide audio track button"),
                StringResource("revanced_hide_audio_track_button_on", "Audio track button is hidden"),
                StringResource("revanced_hide_audio_track_button_off", "Audio track button is shown")
            ),
            SwitchPreference(
                "revanced_hide_latest_posts_ads",
                StringResource("revanced_hide_latest_posts_ads_title", "Hide latest posts"),
                StringResource("revanced_hide_latest_posts_ads_summary_on", "Latest posts are hidden"),
                StringResource("revanced_hide_latest_posts_ads_summary_off", "Latest posts are shown")
            ),
            SwitchPreference(
                "revanced_hide_mix_playlists",
                StringResource("revanced_hide_mix_playlists_title", "Hide mix playlists"),
                StringResource("revanced_hide_mix_playlists_summary_on", "Mix playlists are hidden"),
                StringResource("revanced_hide_mix_playlists_summary_off", "Mix playlists are shown")
            ),
            SwitchPreference(
                "revanced_hide_artist_cards",
                StringResource("revanced_hide_artist_cards_title", "Hide artist cards"),
                StringResource("revanced_hide_artist_cards_on", "Artist cards is hidden"),
                StringResource("revanced_hide_artist_cards_off", "Artist cards is shown")
            ),
        )

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_hide_general_ads",
                StringResource("revanced_hide_general_ads_title", "Hide general ads"),
                StringResource("revanced_hide_general_ads_summary_on", "General ads are hidden"),
                StringResource("revanced_hide_general_ads_summary_off", "General ads are shown")
            ),
            SwitchPreference(
                "revanced_hide_buttoned_ads",
                StringResource("revanced_hide_buttoned_ads_title", "Hide buttoned ad"),
                StringResource("revanced_hide_buttoned_ads_summary_on", "Buttoned ads are hidden"),
                StringResource("revanced_hide_buttoned_ads_summary_off", "Buttoned ads are shown")
            ),
            SwitchPreference(
                "revanced_hide_paid_content_ads",
                StringResource("revanced_hide_paid_content_ads_title", "Hide paid content"),
                StringResource("revanced_hide_paid_content_ads_summary_on", "Paid content is hidden"),
                StringResource("revanced_hide_paid_content_ads_summary_off", "Paid content is shown")
            ),
            SwitchPreference(
                "revanced_hide_self_sponsor_ads",
                StringResource("revanced_hide_self_sponsor_ads_title", "Hide self sponsored cards"),
                StringResource("revanced_hide_self_sponsor_ads_summary_on", "Self sponsored cards are hidden"),
                StringResource("revanced_hide_self_sponsor_ads_summary_off", "Self sponsored cards are shown")
            ),
            PreferenceScreen(
                "revanced_custom_filter_preference_screen",
                StringResource("revanced_custom_filter_preference_screen_title", "Custom filter"),
                listOf(
                    SwitchPreference(
                        "revanced_custom_filter",
                        StringResource(
                            "revanced_custom_filter_title",
                            "Enable custom filter"
                        ),
                        StringResource(
                            "revanced_custom_filter_summary_on",
                            "Custom filter is enabled"
                        ),
                        StringResource(
                            "revanced_custom_filter_summary_off",
                            "Custom filter is disabled"
                        )
                    ),
                    // TODO: This should be a dynamic ListPreference, which does not exist yet
                    TextPreference(
                        "revanced_custom_filter_strings",
                        StringResource("revanced_custom_filter_strings_title", "Custom filter"),
                        StringResource(
                            "revanced_custom_filter_strings_summary",
                            "Filter components by their name separated by a comma"
                        )
                    )
                )
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        adAttributionId = ResourceMappingPatch.resourceMappings.single { it.name == "ad_attribution" }.id

        return PatchResultSuccess()
    }

    internal companion object {
        var adAttributionId: Long = -1

        private const val FILTER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/components/AdsFilter;"
    }
}
