package app.revanced.integrations.patches.components;


import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.StringTrieSearch;


public final class AdsFilter extends Filter {
    private final StringTrieSearch exceptions = new StringTrieSearch();

    public AdsFilter() {
        exceptions.addPatterns(
                "home_video_with_context", // Don't filter anything in the home page video component.
                "related_video_with_context", // Don't filter anything in the related video component.
                "comment_thread", // Don't filter anything in the comments.
                "|comment.", // Don't filter anything in the comments replies.
                "library_recent_shelf"
        );

        final var buttonedAd = new StringFilterGroup(
                SettingsEnum.HIDE_BUTTONED_ADS,
                "_buttoned_layout",
                "full_width_square_image_layout",
                "_ad_with",
                "text_image_button_group_layout",
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
                "carousel_headered_layout",
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

        final var carouselAd = new StringFilterGroup(
                SettingsEnum.HIDE_GENERAL_ADS,
                "carousel_ad"
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

        final var merchandise = new StringFilterGroup(
                SettingsEnum.HIDE_MERCHANDISE_BANNERS,
                "product_carousel"
        );

        final var selfSponsor = new StringFilterGroup(
                SettingsEnum.HIDE_SELF_SPONSOR,
                "cta_shelf_card"
        );

        this.pathFilterGroupList.addAll(
                generalAds,
                buttonedAd,
                merchandise,
                viewProducts,
                selfSponsor,
                webLinkPanel,
                movieAds
        );
        this.identifierFilterGroupList.addAll(carouselAd);
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (exceptions.matches(path))
           return false;

        return super.isFiltered(identifier, path, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
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
