package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class GeneralAdsPatch extends Filter {
    private final String[] IGNORE = {
            "home_video_with_context",
            "related_video_with_context",
            "comment_thread", // skip blocking anything in the comments
            "|comment.", // skip blocking anything in the comments replies
            "download_",
            "library_recent_shelf",
            "playlist_add_to_option_wrapper" // do not block on "add to playlist" flyout menu
    };

    private final BlockRule custom = new CustomBlockRule(
            SettingsEnum.ADREMOVER_CUSTOM_ENABLED,
            SettingsEnum.ADREMOVER_CUSTOM_REMOVAL
    );

    public GeneralAdsPatch() {
        var communityPosts = new BlockRule(SettingsEnum.ADREMOVER_COMMUNITY_POSTS_REMOVAL, "post_base_wrapper");
        var communityGuidelines = new BlockRule(SettingsEnum.ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL, "community_guidelines");
        var subscribersCommunityGuidelines = new BlockRule(SettingsEnum.ADREMOVER_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL, "sponsorships_comments_upsell");
        var compactBanner = new BlockRule(SettingsEnum.ADREMOVER_COMPACT_BANNER_REMOVAL, "compact_banner");
        var inFeedSurvey = new BlockRule(SettingsEnum.ADREMOVER_FEED_SURVEY_REMOVAL, "in_feed_survey");
        var medicalPanel = new BlockRule(SettingsEnum.ADREMOVER_MEDICAL_PANEL_REMOVAL, "medical_panel");
        var paidContent = new BlockRule(SettingsEnum.ADREMOVER_PAID_CONTENT_REMOVAL, "paid_content_overlay");
        var merchandise = new BlockRule(SettingsEnum.ADREMOVER_MERCHANDISE_REMOVAL, "product_carousel");
        var infoPanel = new BlockRule(SettingsEnum.ADREMOVER_INFO_PANEL_REMOVAL, "publisher_transparency_panel", "single_item_information_panel");
        var suggestions = new BlockRule(SettingsEnum.ADREMOVER_SUGGESTIONS_REMOVAL, "horizontal_video_shelf");
        var latestPosts = new BlockRule(SettingsEnum.ADREMOVER_HIDE_LATEST_POSTS, "post_shelf");
        var channelGuidelines = new BlockRule(SettingsEnum.ADREMOVER_HIDE_CHANNEL_GUIDELINES, "channel_guidelines_entry_banner");
        var artistCard = new BlockRule(SettingsEnum.HIDE_ARTIST_CARD, "official_card");
        var selfSponsor = new BlockRule(SettingsEnum.ADREMOVER_SELF_SPONSOR_REMOVAL, "cta_shelf_card");
        var chapterTeaser = new BlockRule(SettingsEnum.ADREMOVER_CHAPTER_TEASER_REMOVAL, "expandable_metadata");
        var graySeparator = new BlockRule(SettingsEnum.ADREMOVER_GRAY_SEPARATOR,
                "cell_divider" // layout residue (gray line above the buttoned ad),
        );
        var buttonedAd = new BlockRule(SettingsEnum.ADREMOVER_BUTTONED_REMOVAL,
                "video_display_full_buttoned_layout",
                "full_width_square_image_layout",
                "_ad_with",
                "landscape_image_wide_button_layout"
        );
        var generalAds = new BlockRule(
                SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL,
                "ads_video_with_context",
                "banner_text_icon",
                "square_image_layout",
                "watch_metadata_app_promo",
                "video_display_full_layout"
        );
        var movieAds = new BlockRule(
                SettingsEnum.ADREMOVER_MOVIE_REMOVAL,
                "browsy_bar",
                "compact_movie",
                "horizontal_movie_shelf",
                "movie_and_show_upsell_card",
                "compact_tvfilm_item"
        );

        this.pathRegister.registerAll(
                generalAds,
                buttonedAd,
                communityPosts,
                paidContent,
                suggestions,
                latestPosts,
                movieAds,
                chapterTeaser,
                communityGuidelines,
                compactBanner,
                inFeedSurvey,
                medicalPanel,
                merchandise,
                infoPanel,
                channelGuidelines,
                artistCard,
                selfSponsor,
                subscribersCommunityGuidelines
        );

        var carouselAd = new BlockRule(SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL,
                "carousel_ad"
        );
        var shorts = new BlockRule(SettingsEnum.ADREMOVER_SHORTS_REMOVAL,
                "reels_player_overlay",
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid"
        );

        this.identifierRegister.registerAll(
                shorts,
                graySeparator,
                carouselAd
        );
    }

    public boolean filter(final String path, final String identifier) {
        BlockResult result;

        if (custom.isEnabled() && custom.check(path).isBlocked())
            result = BlockResult.CUSTOM;
        else if (ReVancedUtils.containsAny(path, IGNORE))
            result = BlockResult.IGNORED;
        else if (pathRegister.contains(path) || identifierRegister.contains(identifier))
            result = BlockResult.DEFINED;
        else
            result = BlockResult.UNBLOCKED;

        log(String.format("%s (ID: %s): %s", result.message, identifier, path));

        return result.filter;
    }

    private enum BlockResult {
        UNBLOCKED(false, "Unblocked"),
        IGNORED(false, "Ignored"),
        DEFINED(true, "Blocked"),
        CUSTOM(true, "Custom");

        final Boolean filter;
        final String message;

        BlockResult(boolean filter, String message) {
            this.filter = filter;
            this.message = message;
        }
    }

    /**
     * Hide a view.
     *
     * @param condition The setting to check for hiding the view.
     * @param view      The view to hide.
     */
    private static void hideView(SettingsEnum condition, View view) {
        if (!condition.getBoolean()) return;

        log("Hiding view with setting: " + condition);

        AdRemoverAPI.HideViewWithLayout1dp(view);
    }

    /**
     * Hide the view, which shows ads in the homepage.
     *
     * @param view The view, which shows ads.
     */
    public static void hideAdAttributionView(View view) {
        hideView(SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL, view);
    }

    /**
     * Hide the view, which shows reels in the homepage.
     *
     * @param view The view, which shows reels.
     */
    public static void hideReelView(View view) {
        hideView(SettingsEnum.ADREMOVER_SHORTS_REMOVAL, view);
    }

    private static void log(String message) {
        LogHelper.printDebug(() -> message);
    }
}
