package app.revanced.extension.youtube.patches.litho;

import static app.revanced.extension.shared.StringRef.str;

import android.app.Dialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.util.List;

import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.shared.patches.litho.FilterGroup.ByteArrayFilterGroup;
import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class AdsFilter extends Filter {
    // region Fullscreen ad
    private static final ByteArrayFilterGroup fullscreenAd = new ByteArrayFilterGroup(
            null,
            "_interstitial"
    );

    // endregion

    private static final String[] PLAYER_POPUP_AD_PANEL_IDS = {
            "PAproduct", // Shopping.
            "jumpahead" // Premium promotion.
    };

    // https://encrypted-tbn0.gstatic.com/shopping?q=abc
    private static final String STORE_BANNER_DOMAIN = "gstatic.com/shopping";
    private static final boolean HIDE_END_SCREEN_STORE_BANNER =
            Settings.HIDE_END_SCREEN_STORE_BANNER.get();

    private final StringTrieSearch exceptions = new StringTrieSearch();

    private final StringFilterGroup promotionBanner;
    private final ByteArrayFilterGroup promotionBannerBuffer;
    private final StringFilterGroup buyMovieAd;
    private final ByteArrayFilterGroup buyMovieAdBuffer;

    public AdsFilter() {
        exceptions.addPatterns(
                "home_video_with_context", // Don't filter anything in the home page video component.
                "related_video_with_context", // Don't filter anything in the related video component.
                "comment_thread", // Don't filter anything in the comments.
                "|comment.", // Don't filter anything in the comments replies.
                "library_recent_shelf"
        );

        // Identifiers.

        final var carouselAd = new StringFilterGroup(
                Settings.HIDE_GENERAL_ADS,
                "carousel_ad"
        );
        addIdentifierCallbacks(carouselAd);

        // Paths.

        final var generalAds = new StringFilterGroup(
                Settings.HIDE_GENERAL_ADS,
                "_ad_with",
                "_buttoned_layout",
                "ads_video_with_context",
                "banner_text_icon",
                "brand_video_shelf",
                "brand_video_singleton",
                "carousel_footered_layout",
                "carousel_headered_layout",
                "compact_landscape_image_layout", // Tablet layout search results.
                "composite_concurrent_carousel_layout",
                "full_width_portrait_image_layout",
                "full_width_square_image_carousel_layout",
                "full_width_square_image_layout",
                "hero_promo_image",
                // text_image_button_group_layout, landscape_image_button_group_layout, full_width_square_image_button_group_layout
                "image_button_group_layout",
                "landscape_image_carousel_layout",
                "landscape_image_wide_button_layout",
                "primetime_promo",
                "product_details",
                "square_image_layout",
                "text_image_button_layout",
                "text_image_no_button_layout", // Tablet layout search results.
                "video_display_button_group_layout",
                "video_display_carousel_button_group_layout",
                "video_display_carousel_buttoned_short_dr_layout",
                "video_display_full_buttoned_short_dr_layout",
                "video_display_full_layout",
                "watch_metadata_app_promo",
                "shopping_timely_shelf." // Injection point below hides the empty space.
        );

        final var movieAds = new StringFilterGroup(
                Settings.HIDE_MOVIES_SECTION,
                "browsy_bar",
                "compact_movie",
                "compact_tvfilm_item",
                "horizontal_movie_shelf",
                "movie_and_show_upsell_card",
                "offer_module_root"
        );

        buyMovieAd = new StringFilterGroup(
                Settings.HIDE_MOVIES_SECTION,
                "video_lockup_with_attachment.e"
        );

        buyMovieAdBuffer =  new ByteArrayFilterGroup(
                null,
                "FEstorefront"
        );

        final var viewProducts = new StringFilterGroup(
                Settings.HIDE_VIEW_PRODUCTS_BANNER,
                "product_item",
                "products_in_video",
                "shopping_overlay.e" // Video player overlay shopping links.
        );

        final var shoppingLinks = new StringFilterGroup(
                Settings.HIDE_SHOPPING_LINKS,
                "shopping_description_shelf.e"
        );

        final var merchandise = new StringFilterGroup(
                Settings.HIDE_MERCHANDISE_BANNERS,
                "product_carousel",
                "shopping_carousel.e" // Channel profile shopping shelf.
        );

        promotionBanner = new StringFilterGroup(
                Settings.HIDE_YOUTUBE_PREMIUM_PROMOTIONS,
                "statement_banner"
        );

        promotionBannerBuffer = new ByteArrayFilterGroup(
                null,
                "img/promos/growth/", // Link, https://www.gstatic.com/youtube/img/promos/growth/ is only used for ads.
                "SPunlimited" // Word associated with Premium, should be unique to differentiate Doodle from ad banner.
        );

        final var selfSponsor = new StringFilterGroup(
                Settings.HIDE_SELF_SPONSOR,
                "cta_shelf_card"
        );

        addPathCallbacks(
                buyMovieAd,
                generalAds,
                merchandise,
                movieAds,
                promotionBanner,
                selfSponsor,
                shoppingLinks,
                viewProducts
        );
    }

    @Override
    public boolean isFiltered(String identifier, String accessibility, String path, byte[] buffer,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == buyMovieAd) {
            return contentIndex == 0 && buyMovieAdBuffer.check(buffer).isFiltered();
        }

        if (matchedGroup == promotionBanner) {
            return contentIndex == 0 && promotionBannerBuffer.check(buffer).isFiltered();
        }

        return !exceptions.matches(path);
    }

    /**
     * Injection point.
     * Called from a different place then the other filters.
     */
    public static void closeFullscreenAd(Object customDialog, @Nullable byte[] buffer) {
        try {
            if (!Settings.HIDE_FULLSCREEN_ADS.get()) {
                return;
            }

            if (buffer == null) {
                Logger.printDebug(() -> "buffer is null");
                return;
            }

            if (fullscreenAd.check(buffer).isFiltered() &&
                    customDialog instanceof Dialog dialog) {
                Logger.printDebug(() -> "Closing fullscreen ad");

                Window window = dialog.getWindow();

                if (window != null) {
                    // Set the dialog size to 0 before closing
                    // If the dialog is not resized to 0, it will remain visible for about a second before closing
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.height = 0;
                    params.width = 0;

                    // Change the size of dialog to 0
                    window.setAttributes(params);

                    // Disable dialog's background dim
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                    // Restore window flags
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);

                    // Restore decorView visibility
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

                // Dismiss dialog
                dialog.dismiss();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "closeFullscreenAd failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static boolean hideAds() {
        return Settings.HIDE_GENERAL_ADS.get();
    }

    /**
     * Injection point.
     */
    public static String hideAds(String osName) {
        return Settings.HIDE_GENERAL_ADS.get()
                ? "Android Automotive"
                : osName;
    }

    /**
     * Hide the view, which shows ads in the homepage.
     *
     * @param view The view, which shows ads.
     */
    public static void hideAdAttributionView(View view) {
        Utils.hideViewBy0dpUnderCondition(Settings.HIDE_GENERAL_ADS, view);
    }

    /**
     * Injection point.
     *
     * @param elementsList List of components of the end screen container.
     * @param protobufList Component (ProtobufList).
     */
    public static void hideEndScreenStoreBanner(List<Object> elementsList, Object protobufList) {
        if (HIDE_END_SCREEN_STORE_BANNER && protobufList.toString().contains(STORE_BANNER_DOMAIN)) {
            Logger.printDebug(() -> "Hiding store banner");
            return;
        }

        elementsList.add(protobufList);
    }

    /**
     * Injection point.
     */
    public static boolean hideGetPremiumView() {
        return Settings.HIDE_YOUTUBE_PREMIUM_PROMOTIONS.get();
    }

    /**
     * Injection point.
     */
    public static boolean hidePlayerPopupAds(String panelId) {
        return Settings.HIDE_PLAYER_POPUP_ADS.get()
                && Utils.containsAny(panelId, PLAYER_POPUP_AD_PANEL_IDS);
    }
}
