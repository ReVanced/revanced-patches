package app.revanced.integrations.youtube.patches.components;

import android.app.Instrumentation;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.StringTrieSearch;

@SuppressWarnings("unused")
public final class AdsFilter extends Filter {
    // region Fullscreen ad
    private static long lastTimeClosedFullscreenAd = 0;
    private static final Instrumentation instrumentation = new Instrumentation();
    private final StringFilterGroup fullscreenAd;

    // endregion

    private final StringTrieSearch exceptions = new StringTrieSearch();
    private final StringFilterGroup shoppingLinks;

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

        final var buttonedAd = new StringFilterGroup(
                Settings.HIDE_BUTTONED_ADS,
                "_buttoned_layout",
                "full_width_square_image_layout",
                "_ad_with",
                "text_image_button_group_layout",
                "video_display_button_group_layout",
                "landscape_image_wide_button_layout",
                "video_display_carousel_button_group_layout"
        );

        final var generalAds = new StringFilterGroup(
                Settings.HIDE_GENERAL_ADS,
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
                Settings.HIDE_MOVIES_SECTION,
                "browsy_bar",
                "compact_movie",
                "horizontal_movie_shelf",
                "movie_and_show_upsell_card",
                "compact_tvfilm_item",
                "offer_module_root"
        );

        final var viewProducts = new StringFilterGroup(
                Settings.HIDE_PRODUCTS_BANNER,
                "product_item",
                "products_in_video"
        );

        shoppingLinks = new StringFilterGroup(
                Settings.HIDE_SHOPPING_LINKS,
                "expandable_list"
        );

        final var webLinkPanel = new StringFilterGroup(
                Settings.HIDE_WEB_SEARCH_RESULTS,
                "web_link_panel"
        );

        final var merchandise = new StringFilterGroup(
                Settings.HIDE_MERCHANDISE_BANNERS,
                "product_carousel"
        );

        final var selfSponsor = new StringFilterGroup(
                Settings.HIDE_SELF_SPONSOR,
                "cta_shelf_card"
        );

        addPathCallbacks(
                generalAds,
                buttonedAd,
                merchandise,
                viewProducts,
                selfSponsor,
                fullscreenAd,
                webLinkPanel,
                shoppingLinks,
                movieAds
        );
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (exceptions.matches(path))
            return false;

        if (matchedGroup == fullscreenAd) {
            if (path.contains("|ImageType|")) closeFullscreenAd();

            return false; // Do not actually filter the fullscreen ad otherwise it will leave a dimmed screen.
        }

        // Check for the index because of likelihood of false positives.
        if (matchedGroup == shoppingLinks && contentIndex != 0)
            return false;

        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }

    /**
     * Hide the view, which shows ads in the homepage.
     *
     * @param view The view, which shows ads.
     */
    public static void hideAdAttributionView(View view) {
        Utils.hideViewBy1dpUnderCondition(Settings.HIDE_GENERAL_ADS, view);
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

        Utils.runOnMainThreadDelayed(() -> instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK), 1000);
    }
}
