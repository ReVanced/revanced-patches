package app.revanced.integrations.adremover;

import android.os.Build;


import androidx.annotation.RequiresApi;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.settings.Settings;

public class LithoAdRemoval {
    private static boolean getBoolean(String key, boolean _default) {
        return SharedPrefHelper.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), SharedPrefHelper.SharedPrefNames.YOUTUBE, key, _default);
    }

    private static boolean isExperimentalInfoPanelRemoval() {
        return getBoolean("experimental_info_panel", true);
    }

    private static boolean isExperimentalMedicalPanelRemoval() {
        return getBoolean("experimental_medical_panel", true);
    }

    private static boolean isExperimentalEmergencyBoxRemoval() {
        return getBoolean("experimental_emergency_box", true);
    }

    private static boolean isExperimentalAdRemoval() {
        return getBoolean("experimental_ad_removal", true);
    }

    private static boolean isExperimentalMerchandiseRemoval() {
        return getBoolean("experimental_merchandise", true);
    }

    private static boolean isExperimentalCommunityPostRemoval() {
        return getBoolean("experimental_community_posts", false);
    }

    private static boolean isExperimentalMovieRemoval() {
        return getBoolean("experimental_movie", true);
    }

    private static boolean isExperimentalCompactBannerRemoval() {
        return getBoolean("experimental_compact_banner", false);
    }

    private static boolean isExperimentalPaidContentRemoval() {
        return getBoolean("experimental_paid_content", true);
    }

    private static boolean isExperimentalCommentsRemoval() {
        return getBoolean("experimental_comments", false);
    }

    private static boolean isInFeedSurvey() {
        return getBoolean("experimental_in_feed_survey", false);
    }

    private static boolean isShortsShelf() {
        return getBoolean("experimental_shorts_shelf", true);
    }

    private static boolean isCommunityGuidelines() {
        return getBoolean("experimental_community_guidelines", true);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean containsAd(String value, ByteBuffer buffer) {
        try {
            if (!(isExperimentalAdRemoval() ||
                    isExperimentalMerchandiseRemoval() ||
                    isExperimentalPaidContentRemoval() || isExperimentalCommunityPostRemoval() ||
                    isExperimentalMovieRemoval() ||
                    isExperimentalCompactBannerRemoval() ||
                    isExperimentalCommentsRemoval() ||
                    isInFeedSurvey() ||
                    isShortsShelf() ||
                    isCommunityGuidelines()) ||
                    value == null ||
                    value.isEmpty()
            ) {
                return false;
            }
            List<String> blockList = new ArrayList<>();
            List<String> bufferBlockList = new ArrayList<>();

            if (isExperimentalAdRemoval()) {
                blockList.add("_ad");
                blockList.add("ad_badge");
                blockList.add("ads_video_with_context");
                blockList.add("text_search_ad_with_description_first");
                blockList.add("shelf_header");
                blockList.add("cell_divider");
                blockList.add("watch_metadata_app_promo");
                blockList.add("reels_player_overlay");

                bufferBlockList.add("ad_cpn");
            }
            if (isExperimentalMovieRemoval()) {
                blockList.add("movie_and_show_upsell_card");
                blockList.add("compact_movie");
                blockList.add("horizontal_movie_shelf");

                bufferBlockList.add("YouTube Movies");
            }

            if (
                    value.contains("related_video_with_context") &&
                            bufferBlockList
                                    .stream()
                                    .anyMatch(StandardCharsets.UTF_8.decode(buffer).toString()::contains)
            ) return true;

            if (isExperimentalMerchandiseRemoval()) {
                blockList.add("product_carousel");
            }
            if (isExperimentalCommunityPostRemoval()) {
                blockList.add("post_base_wrapper");
            }

            if (isExperimentalPaidContentRemoval()) {
                blockList.add("paid_content_overlay");
            }
            if (isExperimentalEmergencyBoxRemoval()) {
                blockList.add("emergency_onebox");
            }
            if (isExperimentalMedicalPanelRemoval()) {
                blockList.add("medical_panel");
            }
            if (isExperimentalInfoPanelRemoval()) {
                blockList.add("single_item_information_panel");
                blockList.add("publisher_transparency_panel");
            }
            if (isExperimentalCompactBannerRemoval()) {
                blockList.add("compact_banner");
            }
            if (isExperimentalCommentsRemoval()) {
                blockList.add("comments_composite_entry_point");
            }
            if (isInFeedSurvey()) {
                blockList.add("in_feed_survey");
            }
            if (isShortsShelf()) {
                blockList.add("shorts_shelf");
            }
            if (isCommunityGuidelines()) {
                blockList.add("community_guidelines");
            }

            if (containsAny(value,
                    "home_video_with_context",
                    "related_video_with_context",
                    "search_video_with_context",
                    "menu",
                    "root",
                    "-count",
                    "-space",
                    "-button"
            )) return false;

            if (blockList.stream().anyMatch(value::contains)) {
                LogHelper.debug("TemplateBlocked", value);
                return true;
            }

            if (!SettingsEnum.DEBUG_BOOLEAN.getBoolean()) return false;
            if (value.contains("related_video_with_context")) {
                LogHelper.debug("Template", value + " | " + bytesToHex(buffer.array()));
                return false;
            }
            LogHelper.debug("Template", value);
            return false;
        } catch (
                Exception ex) {
            LogHelper.printException("Template", ex.getMessage(), ex);
            return false;
        }

    }

    private static boolean containsAny(String value, String... targets) {
        for (String string : targets)
            if (value.contains(string)) return true;
        return false;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            builder.append(String.format("%02x", b));
        return builder.toString();
    }
}
