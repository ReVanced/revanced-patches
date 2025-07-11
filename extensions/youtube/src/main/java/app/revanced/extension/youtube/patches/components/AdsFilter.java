package app.revanced.extension.youtube.patches.components;

import static app.revanced.extension.shared.StringRef.str;

import android.app.Instrumentation;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.StringTrieSearch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class AdsFilter extends Filter {
    // region Fullscreen ad
    private static volatile long lastTimeClosedFullscreenAd;
    private static final Instrumentation instrumentation = new Instrumentation();
    private final StringFilterGroup fullscreenAd;

    // endregion

    // https://encrypted-tbn0.gstatic.com/shopping?q=abc
    private static final String STORE_BANNER_DOMAIN = "gstatic.com/shopping";
    private static final boolean HIDE_END_SCREEN_STORE_BANNER =
            Settings.HIDE_END_SCREEN_STORE_BANNER.get();

    private final StringTrieSearch exceptions = new StringTrieSearch();

    private final StringFilterGroup playerShoppingShelf;
    private final ByteArrayFilterGroup playerShoppingShelfBuffer;


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

        fullscreenAd = new StringFilterGroup(
                Settings.HIDE_FULLSCREEN_ADS,
                "_interstitial"
        );

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
                "landscape_image_wide_button_layout",
                "primetime_promo",
                "product_details",
                "square_image_layout",
                "statement_banner",
                "text_image_button_layout",
                "text_image_no_button_layout", // Tablet layout search results.
                "video_display_button_group_layout",
                "video_display_carousel_button_group_layout",
                "video_display_carousel_buttoned_short_dr_layout",
                "video_display_full_buttoned_short_dr_layout",
                "video_display_full_layout",
                "watch_metadata_app_promo"
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

        final var viewProducts = new StringFilterGroup(
                Settings.HIDE_VIEW_PRODUCTS_BANNER,
                "product_item",
                "products_in_video",
                "shopping_overlay.eml" // Video player overlay shopping links.
        );

        final var shoppingLinks = new StringFilterGroup(
                Settings.HIDE_SHOPPING_LINKS,
                "shopping_description_shelf.eml"
        );

        playerShoppingShelf = new StringFilterGroup(
                Settings.HIDE_CREATOR_STORE_SHELF,
                "horizontal_shelf.eml"
        );

        playerShoppingShelfBuffer = new ByteArrayFilterGroup(
                null,
                "shopping_item_card_list"
        );

        final var webLinkPanel = new StringFilterGroup(
                Settings.HIDE_WEB_SEARCH_RESULTS,
                "web_link_panel"
        );

        final var merchandise = new StringFilterGroup(
                Settings.HIDE_MERCHANDISE_BANNERS,
                "product_carousel",
                "shopping_carousel.eml" // Channel profile shopping shelf.
        );

        final var selfSponsor = new StringFilterGroup(
                Settings.HIDE_SELF_SPONSOR,
                "cta_shelf_card"
        );

        addPathCallbacks(
                fullscreenAd,
                generalAds,
                merchandise,
                movieAds,
                playerShoppingShelf,
                selfSponsor,
                shoppingLinks,
                viewProducts,
                webLinkPanel
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == playerShoppingShelf) {
            return contentIndex == 0 && playerShoppingShelfBuffer.check(protobufBufferArray).isFiltered();
        }

        if (exceptions.matches(path)) {
            return false;
        }

        if (matchedGroup == fullscreenAd) {
            if (path.contains("|ImageType|")) closeFullscreenAd();

            // Do not actually filter the fullscreen ad otherwise it will leave a dimmed screen.
            return false;
        }

        return true;
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
     * Hide the view, which shows ads in the homepage.
     *
     * @param view The view, which shows ads.
     */
    public static void hideAdAttributionView(View view) {
        Utils.hideViewBy0dpUnderCondition(Settings.HIDE_GENERAL_ADS, view);
    }

    /**
     * Close the fullscreen ad.
     * <p>
     * The strategy is to send a back button event to the app to close the fullscreen ad using the back button event.
     */
    private static void closeFullscreenAd() {
        final var currentTime = System.currentTimeMillis();

        // Prevent spamming the back button.
        if (currentTime - lastTimeClosedFullscreenAd < 10000) return;
        lastTimeClosedFullscreenAd = currentTime;

        Logger.printDebug(() -> "Closing fullscreen ad");

        Utils.runOnMainThreadDelayed(() -> {
            // Must run off main thread (Odd, but whatever).
            Utils.runOnBackgroundThread(() -> {
                try {
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                } catch (Exception ex) {
                    // Injecting user events on Android 10+ requires the manifest to include
                    // INJECT_EVENTS, and it's usage is heavily restricted
                    // and requires the user to manually approve the permission in the device settings.
                    //
                    // And no matter what, permissions cannot be added for root installations
                    // as manifest changes are ignored for mount installations.
                    //
                    // Instead, catch the SecurityException and turn off hide full screen ads
                    // since this functionality does not work for these devices.
                    Logger.printInfo(() -> "Could not inject back button event", ex);
                    Settings.HIDE_FULLSCREEN_ADS.save(false);
                    Utils.showToastLong(str("revanced_hide_fullscreen_ads_feature_not_available_toast"));
                }
            });
        }, 1000);
    }
}
