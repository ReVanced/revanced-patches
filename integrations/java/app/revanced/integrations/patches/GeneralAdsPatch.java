package app.revanced.integrations.patches;

import android.view.View;
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
            SettingsEnum.CUSTOM_FILTER,
            SettingsEnum.CUSTOM_FILTER_STRINGS
    );

    public GeneralAdsPatch() {
        var communityPosts = new BlockRule(SettingsEnum.HIDE_COMMUNITY_POSTS, "post_base_wrapper");
        var communityGuidelines = new BlockRule(SettingsEnum.HIDE_COMMUNITY_GUIDELINES, "community_guidelines");
        var subscribersCommunityGuidelines = new BlockRule(SettingsEnum.HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES, "sponsorships_comments_upsell");
        var channelMemberShelf = new BlockRule(SettingsEnum.HIDE_CHANNEL_MEMBER_SHELF, "member_recognition_shelf");
        var compactBanner = new BlockRule(SettingsEnum.HIDE_COMPACT_BANNER, "compact_banner");
        var inFeedSurvey = new BlockRule(SettingsEnum.HIDE_FEED_SURVEY, "in_feed_survey", "slimline_survey");
        var medicalPanel = new BlockRule(SettingsEnum.HIDE_MEDICAL_PANELS, "medical_panel");
        var merchandise = new BlockRule(SettingsEnum.HIDE_MERCHANDISE_BANNERS, "product_carousel");
        var infoPanel = new BlockRule(SettingsEnum.HIDE_HIDE_INFO_PANELS, "publisher_transparency_panel", "single_item_information_panel");
        var channelGuidelines = new BlockRule(SettingsEnum.HIDE_HIDE_CHANNEL_GUIDELINES, "channel_guidelines_entry_banner");
        var audioTrackButton = new BlockRule(SettingsEnum.HIDE_AUDIO_TRACK_BUTTON, "multi_feed_icon_button");
        var artistCard = new BlockRule(SettingsEnum.HIDE_ARTIST_CARDS, "official_card");
        var chapterTeaser = new BlockRule(SettingsEnum.HIDE_CHAPTER_TEASER, "expandable_metadata", "macro_markers_carousel");
        var viewProducts = new BlockRule(SettingsEnum.HIDE_PRODUCTS_BANNER, "product_item", "products_in_video");
        var webLinkPanel = new BlockRule(SettingsEnum.HIDE_WEB_SEARCH_RESULTS, "web_link_panel");
        var channelBar = new BlockRule(SettingsEnum.HIDE_CHANNEL_BAR, "channel_bar");
        var relatedVideos = new BlockRule(SettingsEnum.HIDE_RELATED_VIDEOS, "fullscreen_related_videos");
        var quickActions = new BlockRule(SettingsEnum.HIDE_QUICK_ACTIONS, "quick_actions");
        var imageShelf = new BlockRule(SettingsEnum.HIDE_IMAGE_SHELF, "image_shelf");
        var graySeparator = new BlockRule(SettingsEnum.HIDE_GRAY_SEPARATOR,
                "cell_divider" // layout residue (gray line above the buttoned ad),
        );
        var paidContent = new BlockRule(SettingsEnum.HIDE_PAID_CONTENT, "paid_content_overlay");
        var latestPosts = new BlockRule(SettingsEnum.HIDE_HIDE_LATEST_POSTS, "post_shelf");
        var selfSponsor = new BlockRule(SettingsEnum.HIDE_SELF_SPONSOR, "cta_shelf_card");
        var buttonedAd = new BlockRule(SettingsEnum.HIDE_BUTTONED_ADS,
                "_buttoned_layout",
                "full_width_square_image_layout",
                "_ad_with",
                "video_display_button_group_layout",
                "landscape_image_wide_button_layout"
        );
        var generalAds = new BlockRule(
                SettingsEnum.HIDE_GENERAL_ADS,
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
                "product_details",
                "full_width_portrait_image_layout",
                "brand_video_shelf"
        );
        var movieAds = new BlockRule(
                SettingsEnum.HIDE_MOVIES_SECTION,
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
                audioTrackButton,
                artistCard,
                selfSponsor,
                webLinkPanel,
                imageShelf,
                subscribersCommunityGuidelines,
                channelMemberShelf
        );

        var carouselAd = new BlockRule(SettingsEnum.HIDE_GENERAL_ADS,
                "carousel_ad"
        );
        var shorts = new BlockRule(SettingsEnum.HIDE_SHORTS,
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

        ReVancedUtils.HideViewByLayoutParams(view);
    }

    /**
     * Hide the view, which shows ads in the homepage.
     *
     * @param view The view, which shows ads.
     */
    public static void hideAdAttributionView(View view) {
        hideView(SettingsEnum.HIDE_GENERAL_ADS, view);
    }

    /**
     * Hide the view, which shows reels in the homepage.
     *
     * @param view The view, which shows reels.
     */
    public static void hideReelView(View view) {
        hideView(SettingsEnum.HIDE_SHORTS, view);
    }

}
