package app.revanced.integrations.patches.components;


import android.view.View;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;


public final class AdsFilter extends Filter {
    private final String[] exceptions;

    private final CustomFilterGroup custom;

    public AdsFilter() {
        exceptions = new String[]{
                "home_video_with_context",
                "related_video_with_context",
                "comment_thread", // skip filtering anything in the comments
                "|comment.", // skip filtering anything in the comments replies
                "library_recent_shelf",
        };

        custom = new CustomFilterGroup(
                SettingsEnum.CUSTOM_FILTER,
                SettingsEnum.CUSTOM_FILTER_STRINGS
        );

        final var communityPosts = new StringFilterGroup(
                SettingsEnum.HIDE_COMMUNITY_POSTS,
                "post_base_wrapper"
        );

        final var communityGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_COMMUNITY_GUIDELINES,
                "community_guidelines"
        );

        final var subscribersCommunityGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES,
                "sponsorships_comments_upsell"
        );


        final var channelMemberShelf = new StringFilterGroup(
                SettingsEnum.HIDE_CHANNEL_MEMBER_SHELF,
                "member_recognition_shelf"
        );

        final var compactBanner = new StringFilterGroup(
                SettingsEnum.HIDE_COMPACT_BANNER,
                "compact_banner"
        );

        final var inFeedSurvey = new StringFilterGroup(
                SettingsEnum.HIDE_FEED_SURVEY,
                "in_feed_survey",
                "slimline_survey"
        );

        final var medicalPanel = new StringFilterGroup(
                SettingsEnum.HIDE_MEDICAL_PANELS,
                "medical_panel"
        );

        final var paidContent = new StringFilterGroup(
                SettingsEnum.HIDE_PAID_CONTENT,
                "paid_content_overlay"
        );

        final var merchandise = new StringFilterGroup(
                SettingsEnum.HIDE_MERCHANDISE_BANNERS,
                "product_carousel"
        );

        final var infoPanel = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_INFO_PANELS,
                "publisher_transparency_panel",
                "single_item_information_panel"
        );

        final var latestPosts = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_LATEST_POSTS,
                "post_shelf"
        );

        final var channelGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_CHANNEL_GUIDELINES,
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
                SettingsEnum.HIDE_SELF_SPONSOR,
                "cta_shelf_card"
        );

        final var chapterTeaser = new StringFilterGroup(
                SettingsEnum.HIDE_CHAPTER_TEASER,
                "expandable_metadata",
                "macro_markers_carousel"
        );

        final var viewProducts = new StringFilterGroup(
                SettingsEnum.HIDE_PRODUCTS_BANNER,
                "product_item",
                "products_in_video"
        );

        final var webLinkPanel = new StringFilterGroup(
                SettingsEnum.HIDE_WEB_SEARCH_RESULTS,
                "web_link_panel"
        );

        final var channelBar = new StringFilterGroup(
                SettingsEnum.HIDE_CHANNEL_BAR,
                "channel_bar"
        );

        final var relatedVideos = new StringFilterGroup(
                SettingsEnum.HIDE_RELATED_VIDEOS,
                "fullscreen_related_videos"
        );

        final var quickActions = new StringFilterGroup(
                SettingsEnum.HIDE_QUICK_ACTIONS,
                "quick_actions"
        );

        final var imageShelf = new StringFilterGroup(
                SettingsEnum.HIDE_IMAGE_SHELF,
                "image_shelf"
        );

        final var graySeparator = new StringFilterGroup(
                SettingsEnum.HIDE_GRAY_SEPARATOR,
                "cell_divider" // layout residue (gray line above the buttoned ad),
        );

        final var buttonedAd = new StringFilterGroup(
                SettingsEnum.HIDE_BUTTONED_ADS,
                "_buttoned_layout",
                "full_width_square_image_layout",
                "_ad_with",
                "video_display_button_group_layout",
                "landscape_image_wide_button_layout"
        );

        final var generalAds = new StringFilterGroup(
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

        final var movieAds = new StringFilterGroup(
                SettingsEnum.HIDE_MOVIES_SECTION,
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
                SettingsEnum.HIDE_GENERAL_ADS,
                "carousel_ad"
        );

        this.identifierFilterGroups.addAll(
                graySeparator,
                carouselAd
        );
    }

    @Override
    public boolean isFiltered(final String path, final String identifier, final byte[] _protobufBufferArray) {
        FilterResult result;

        if (custom.isEnabled() && custom.check(path).isFiltered())
            result = FilterResult.CUSTOM;
        else if (ReVancedUtils.containsAny(path, exceptions))
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
     * Hide the view, which shows ads in the homepage.
     *
     * @param view The view, which shows ads.
     */
    public static void hideAdAttributionView(View view) {
        ReVancedUtils.hideViewBy1dpUnderCondition(SettingsEnum.HIDE_GENERAL_ADS, view);
    }
}
