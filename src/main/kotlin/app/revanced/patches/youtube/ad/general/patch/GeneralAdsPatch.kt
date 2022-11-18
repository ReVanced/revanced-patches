package app.revanced.patches.youtube.ad.general.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.ad.general.annotations.GeneralAdsCompatibility
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Patch
@DependsOn(dependencies = [FixLocaleConfigErrorPatch::class, LithoFilterPatch::class, SettingsPatch::class])
@Name("general-ads")
@Description("Removes general ads.")
@GeneralAdsCompatibility
@Version("0.0.1")
class GeneralAdsPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_home_ads_removal",
                StringResource("revanced_home_ads_removal_title", "Remove home ads"),
                true,
                StringResource("revanced_home_ads_removal_summary_on", "Home ads are hidden"),
                StringResource("revanced_home_ads_removal_summary_off", "Home ads are shown")
            ),
            SwitchPreference(
                "revanced_adremover_ad_removal",
                StringResource("revanced_adremover_ad_removal_enabled_title", "Remove general ads"),
                true,
                StringResource("revanced_adremover_ad_removal_enabled_summary_on", "General ads are hidden"),
                StringResource("revanced_adremover_ad_removal_enabled_summary_off", "General ads are shown")
            ),
            SwitchPreference(
                "revanced_adremover_merchandise",
                StringResource("revanced_adremover_merchandise_enabled_title", "Remove merchandise banners"),
                true,
                StringResource("revanced_adremover_merchandise_enabled_summary_on", "Merchandise banners are hidden"),
                StringResource("revanced_adremover_merchandise_enabled_summary_off", "Merchandise banners are shown")
            ),
            SwitchPreference(
                "revanced_adremover_community_posts_removal",
                StringResource("revanced_adremover_community_posts_enabled_title", "Remove community posts"),
                false,
                StringResource("revanced_adremover_community_posts_enabled_summary_on", "Community posts are hidden"),
                StringResource("revanced_adremover_community_posts_enabled_summary_off", "Community posts are shown")
            ),
            SwitchPreference(
                "revanced_adremover_compact_banner_removal",
                StringResource("revanced_adremover_compact_banner_enabled_title", "Remove compact banners"),
                true,
                StringResource("revanced_adremover_compact_banner_enabled_summary_on", "Compact banners are hidden"),
                StringResource("revanced_adremover_compact_banner_enabled_summary_off", "Compact banners are shown")
            ),
            SwitchPreference(
                "revanced_adremover_movie",
                StringResource("revanced_adremover_movie_enabled_title", "Remove movies section"),
                true,
                StringResource("revanced_adremover_movie_enabled_summary_on", "Movies section is hidden"),
                StringResource("revanced_adremover_movie_enabled_summary_off", "Movies section is shown")
            ),
            SwitchPreference(
                "revanced_adremover_feed_survey",
                StringResource("revanced_adremover_feed_survey_enabled_title", "Remove feed surveys"),
                true,
                StringResource("revanced_adremover_feed_survey_enabled_summary_on", "Feed surveys are hidden"),
                StringResource("revanced_adremover_feed_survey_enabled_summary_off", "Feed surveys are shown")
            ),
            SwitchPreference(
                "revanced_adremover_shorts",
                StringResource("revanced_adremover_shorts_enabled_title", "Remove shorts"),
                true,
                StringResource("revanced_adremover_shorts_enabled_summary_on", "Shorts are hidden"),
                StringResource("revanced_adremover_shorts_enabled_summary_off", "Shorts are shown")
            ),
            SwitchPreference(
                "revanced_adremover_community_guidelines",
                StringResource("revanced_adremover_community_guidelines_enabled_title", "Remove community guidelines"),
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
                "revanced_adremover_emergency_box_removal",
                StringResource("revanced_adremover_emergency_box_enabled_title", "Remove emergency boxes"),
                true,
                StringResource("revanced_adremover_emergency_box_enabled_summary_on", "Emergency boxes are hidden"),
                StringResource("revanced_adremover_emergency_box_enabled_summary_off", "Emergency boxes are shown")
            ),
            SwitchPreference(
                "revanced_adremover_info_panel",
                StringResource("revanced_adremover_info_panel_enabled_title", "Remove info panels"),
                true,
                StringResource("revanced_adremover_info_panel_enabled_summary_on", "Info panels are hidden"),
                StringResource("revanced_adremover_info_panel_enabled_summary_off", "Info panels are shown")
            ),
            SwitchPreference(
                "revanced_adremover_medical_panel",
                StringResource("revanced_adremover_medical_panel_enabled_title", "Remove medical panels"),
                true,
                StringResource("revanced_adremover_medical_panel_enabled_summary_on", "Medical panels are hidden"),
                StringResource("revanced_adremover_medical_panel_enabled_summary_off", "Medical panels are shown")
            ),
            SwitchPreference(
                "revanced_adremover_paid_content",
                StringResource("revanced_adremover_paid_content_enabled_title", "Remove paid content"),
                true,
                StringResource("revanced_adremover_paid_content_enabled_summary_on", "Paid content is hidden"),
                StringResource("revanced_adremover_paid_content_enabled_summary_off", "Paid content is shown")
            ),
            SwitchPreference(
                "revanced_adremover_hide_suggestions",
                StringResource("revanced_adremover_hide_suggestions_enabled_title", "Hide suggestions"),
                true,
                StringResource("revanced_adremover_hide_suggestions_enabled_summary_on", "Suggestions are hidden"),
                StringResource("revanced_adremover_hide_suggestions_enabled_summary_off", "Suggestions are shown")
            ),
            SwitchPreference(
                "revanced_adremover_hide_latest_posts",
                StringResource("revanced_adremover_hide_latest_posts_enabled_title", "Hide latest posts"),
                true,
                StringResource("revanced_adremover_hide_latest_posts_enabled_summary_on", "Latest posts are hidden"),
                StringResource("revanced_adremover_hide_latest_posts_enabled_summary_off", "Latest posts are shown")
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
                "revanced_adremover_self_sponsor",
                StringResource("revanced_adremover_self_sponsor_enabled_title", "Hide self sponsored cards"),
                true,
                StringResource("revanced_adremover_self_sponsor_enabled_summary_on", "Self sponsored cards are hidden"),
                StringResource("revanced_adremover_self_sponsor_enabled_summary_off", "Self sponsored cards are shown")
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
            )
        )

        return PatchResultSuccess()
    }
}
