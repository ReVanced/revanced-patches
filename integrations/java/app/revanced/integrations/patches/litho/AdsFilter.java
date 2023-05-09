package app.revanced.integrations.patches.litho;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class AdsFilter extends Filter {
    private final String[] EXCEPTIONS;

    private final CustomFilterGroup custom;

    public AdsFilter() {
        EXCEPTIONS = new String[]{
                "home_video_with_context",
                "related_video_with_context",
                "comment_thread", // skip filtering anything in the comments
                "|comment.", // skip filtering anything in the comments replies
                "library_recent_shelf",
        };

        custom = new CustomFilterGroup(
                SettingsEnum.ADREMOVER_CUSTOM_ENABLED,
                SettingsEnum.ADREMOVER_CUSTOM_REMOVAL
        );

        final var communityPosts = new StringFilterGroup(
                SettingsEnum.ADREMOVER_COMMUNITY_POSTS_REMOVAL,
                "post_base_wrapper"
        );

        final var communityGuidelines = new StringFilterGroup(
                SettingsEnum.ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL,
                "community_guidelines"
        );

        final var subscribersCommunityGuidelines = new StringFilterGroup(
                SettingsEnum.ADREMOVER_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL,
                "sponsorships_comments_upsell"
        );


        final var channelMemberShelf = new StringFilterGroup(
                SettingsEnum.ADREMOVER_CHANNEL_MEMBER_SHELF_REMOVAL,
                "member_recognition_shelf"
        );

        final var compactBanner = new StringFilterGroup(
                SettingsEnum.ADREMOVER_COMPACT_BANNER_REMOVAL,
                "compact_banner"
        );

        final var inFeedSurvey = new StringFilterGroup(
                SettingsEnum.ADREMOVER_FEED_SURVEY_REMOVAL,
                "in_feed_survey",
                "slimline_survey"
        );

        final var medicalPanel = new StringFilterGroup(
                SettingsEnum.ADREMOVER_MEDICAL_PANEL_REMOVAL,
                "medical_panel"
        );

        final var paidContent = new StringFilterGroup(
                SettingsEnum.ADREMOVER_PAID_CONTENT_REMOVAL,
                "paid_content_overlay"
        );

        final var merchandise = new StringFilterGroup(
                SettingsEnum.ADREMOVER_MERCHANDISE_REMOVAL,
                "product_carousel"
        );

        final var infoPanel = new StringFilterGroup(
                SettingsEnum.ADREMOVER_INFO_PANEL_REMOVAL,
                "publisher_transparency_panel",
                "single_item_information_panel"
        );

        final var latestPosts = new StringFilterGroup(
                SettingsEnum.ADREMOVER_HIDE_LATEST_POSTS,
                "post_shelf"
        );

        final var channelGuidelines = new StringFilterGroup(
                SettingsEnum.ADREMOVER_HIDE_CHANNEL_GUIDELINES,
                "channel_guidelines_entry_banner"
        );

        final var audioTrackButton = new StringFilterGroup(
                SettingsEnum.HIDE_AUDIO_TRACK_BUTTON,
                "multi_feed_icon_button"
        );

        final var artistCard = new StringFilterGroup(
                SettingsEnum.HIDE_ARTIST_CARDS,
                "official_card"
        );

        final var selfSponsor = new StringFilterGroup(
                SettingsEnum.ADREMOVER_SELF_SPONSOR_REMOVAL,
                "cta_shelf_card"
        );

        final var chapterTeaser = new StringFilterGroup(
                SettingsEnum.ADREMOVER_CHAPTER_TEASER_REMOVAL,
                "expandable_metadata",
                "macro_markers_carousel"
        );

        final var viewProducts = new StringFilterGroup(
                SettingsEnum.ADREMOVER_VIEW_PRODUCTS,
                "product_item",
                "products_in_video"
        );

        final var webLinkPanel = new StringFilterGroup(
                SettingsEnum.ADREMOVER_WEB_SEARCH_RESULTS,
                "web_link_panel"
        );

        final var channelBar = new StringFilterGroup(
                SettingsEnum.ADREMOVER_CHANNEL_BAR,
                "channel_bar"
        );

        final var relatedVideos = new StringFilterGroup(
                SettingsEnum.ADREMOVER_RELATED_VIDEOS,
                "fullscreen_related_videos"
        );

        final var quickActions = new StringFilterGroup(
                SettingsEnum.ADREMOVER_QUICK_ACTIONS,
                "quick_actions"
        );

        final var imageShelf = new StringFilterGroup(
                SettingsEnum.ADREMOVER_IMAGE_SHELF,
                "image_shelf"
        );

        final var graySeparator = new StringFilterGroup(
                SettingsEnum.ADREMOVER_GRAY_SEPARATOR,
                "cell_divider" // layout residue (gray line above the buttoned ad),
        );

        final var buttonedAd = new StringFilterGroup(
                SettingsEnum.ADREMOVER_BUTTONED_REMOVAL,
                "_buttoned_layout",
                "full_width_square_image_layout",
                "_ad_with",
                "video_display_button_group_layout",
                "landscape_image_wide_button_layout"
        );

        final var generalAds = new StringFilterGroup(
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
                "product_details",
                "full_width_portrait_image_layout",
                "brand_video_shelf"
        );

        final var movieAds = new StringFilterGroup(
                SettingsEnum.ADREMOVER_MOVIE_REMOVAL,
                "browsy_bar",
                "compact_movie",
                "horizontal_movie_shelf",
                "movie_and_show_upsell_card",
                "compact_tvfilm_item",
                "offer_module_root"
        );

        this.pathFilterGroups.addAll(
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

        final var carouselAd = new StringFilterGroup(
                SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL,
                "carousel_ad"
        );

        final var shorts = new StringFilterGroup(
                SettingsEnum.ADREMOVER_SHORTS_REMOVAL,
                "reels_player_overlay",
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid"
        );

        this.identifierFilterGroups.addAll(
                shorts,
                graySeparator,
                carouselAd
        );
    }

    @Override
    public boolean isFiltered(final String path, final String identifier, final byte[] _protobufBufferArray) {
        FilterResult result;

        if (custom.isEnabled() && custom.contains(path).isFiltered())
            result = FilterResult.CUSTOM;
        else if (ReVancedUtils.containsAny(path, EXCEPTIONS))
            result = FilterResult.EXCEPTION;
        else if (pathFilterGroups.contains(path) || identifierFilterGroups.contains(identifier))
            result = FilterResult.FILTERED;
        else
            result = FilterResult.UNFILTERED;

        LogHelper.printDebug(() -> String.format("%s (ID: %s): %s", result.message, identifier, path));

        return result.filter;
    }

    private enum FilterResult {
        UNFILTERED(false, "Unfiltered"),
        EXCEPTION(false, "Exception"),
        FILTERED(true, "Filtered"),
        CUSTOM(true, "Custom");

        final Boolean filter;
        final String message;

        FilterResult(boolean filter, String message) {
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
