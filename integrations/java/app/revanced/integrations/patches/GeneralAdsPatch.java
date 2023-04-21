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
            "library_recent_shelf",
    };

    private final BlockRule custom = new CustomBlockRule(
            SettingsEnum.ADREMOVER_CUSTOM_ENABLED,
            SettingsEnum.ADREMOVER_CUSTOM_REMOVAL
    );

    public GeneralAdsPatch() {
        var communityPosts = new BlockRule(SettingsEnum.ADREMOVER_COMMUNITY_POSTS_REMOVAL, "post_base_wrapper");
        var communityGuidelines = new BlockRule(SettingsEnum.ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL, "community_guidelines");
        var subscribersCommunityGuidelines = new BlockRule(SettingsEnum.ADREMOVER_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL, "sponsorships_comments_upsell");
        var channelMemberShelf = new BlockRule(SettingsEnum.ADREMOVER_CHANNEL_MEMBER_SHELF_REMOVAL, "member_recognition_shelf");
        var compactBanner = new BlockRule(SettingsEnum.ADREMOVER_COMPACT_BANNER_REMOVAL, "compact_banner");
        var inFeedSurvey = new BlockRule(SettingsEnum.ADREMOVER_FEED_SURVEY_REMOVAL, "in_feed_survey", "slimline_survey");
        var medicalPanel = new BlockRule(SettingsEnum.ADREMOVER_MEDICAL_PANEL_REMOVAL, "medical_panel");
        var paidContent = new BlockRule(SettingsEnum.ADREMOVER_PAID_CONTENT_REMOVAL, "paid_content_overlay");
        var merchandise = new BlockRule(SettingsEnum.ADREMOVER_MERCHANDISE_REMOVAL, "product_carousel");
        var infoPanel = new BlockRule(SettingsEnum.ADREMOVER_INFO_PANEL_REMOVAL, "publisher_transparency_panel", "single_item_information_panel");
        var latestPosts = new BlockRule(SettingsEnum.ADREMOVER_HIDE_LATEST_POSTS, "post_shelf");
        var channelGuidelines = new BlockRule(SettingsEnum.ADREMOVER_HIDE_CHANNEL_GUIDELINES, "channel_guidelines_entry_banner");
        var artistCard = new BlockRule(SettingsEnum.HIDE_ARTIST_CARDS, "official_card");
        var selfSponsor = new BlockRule(SettingsEnum.ADREMOVER_SELF_SPONSOR_REMOVAL, "cta_shelf_card");
        var chapterTeaser = new BlockRule(SettingsEnum.ADREMOVER_CHAPTER_TEASER_REMOVAL, "expandable_metadata", "macro_markers_carousel");
        var viewProducts = new BlockRule(SettingsEnum.ADREMOVER_VIEW_PRODUCTS, "product_item", "products_in_video");
        var webLinkPanel = new BlockRule(SettingsEnum.ADREMOVER_WEB_SEARCH_RESULTS, "web_link_panel");
        var channelBar = new BlockRule(SettingsEnum.ADREMOVER_CHANNEL_BAR, "channel_bar");
        var relatedVideos = new BlockRule(SettingsEnum.ADREMOVER_RELATED_VIDEOS, "fullscreen_related_videos");
        var quickActions = new BlockRule(SettingsEnum.ADREMOVER_QUICK_ACTIONS, "quick_actions");
        var imageShelf = new BlockRule(SettingsEnum.ADREMOVER_IMAGE_SHELF, "image_shelf");
        var graySeparator = new BlockRule(SettingsEnum.ADREMOVER_GRAY_SEPARATOR,
                "cell_divider" // layout residue (gray line above the buttoned ad),
        );
        var buttonedAd = new BlockRule(SettingsEnum.ADREMOVER_BUTTONED_REMOVAL,
                "_buttoned_layout",
                "full_width_square_image_layout",
                "_ad_with",
                "video_display_button_group_layout",
                "landscape_image_wide_button_layout"
        );
        var generalAds = new BlockRule(
                SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL,
                "ads_video_with_context",
                "banner_text_icon",
                "square_image_layout",
                "watch_metadata_app_promo",
                "video_display_full_layout",
                "hero_promo_image",
                "statement_banner",
                "carousel_footered_layout",
                "text_image_button_layout",
                "primetime_promo",
                "feature_grid_interstitial",
                "product_details",
                "brand_video_shelf"
        );
        var movieAds = new BlockRule(
                SettingsEnum.ADREMOVER_MOVIE_REMOVAL,
                "browsy_bar",
                "compact_movie",
                "horizontal_movie_shelf",
                "movie_and_show_upsell_card",
                "compact_tvfilm_item",
                "offer_module_root"
        );

        this.pathRegister.registerAll(
                generalAds,
                buttonedAd,
                channelBar,
                communityPosts,
                paidContent,
                latestPosts,
                movieAds,
                chapterTeaser,
                communityGuidelines,
                quickActions,
                relatedVideos,
                compactBanner,
                inFeedSurvey,
                viewProducts,
                medicalPanel,
                merchandise,
                infoPanel,
                channelGuidelines,
                artistCard,
                selfSponsor,
                webLinkPanel,
                imageShelf,
                subscribersCommunityGuidelines,
                channelMemberShelf
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

        LogHelper.printDebug(() -> String.format("%s (ID: %s): %s", result.message, identifier, path));

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

        LogHelper.printDebug(() -> "Hiding view with setting: " + condition);

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

}
