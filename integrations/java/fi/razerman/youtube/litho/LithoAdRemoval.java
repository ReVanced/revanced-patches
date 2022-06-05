package fi.razerman.youtube.litho;

import android.util.Log;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fi.razerman.youtube.Helpers.SharedPrefs;
import fi.razerman.youtube.XGlobals;

public class LithoAdRemoval {
    private static final byte[] endRelatedPageAd = {112, 97, 103, 101, 97, 100};

    private static boolean getBoolean(String key, boolean _default) {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), key, _default);
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

    public static boolean isExperimentalAdRemoval() {
        return getBoolean("experimental_ad_removal", true);
    }

    public static boolean isExperimentalMerchandiseRemoval() {
        return getBoolean("experimental_merchandise", true);
    }

    public static boolean isExperimentalCommunityPostRemoval() {
        return getBoolean("experimental_community_posts", false);
    }

    public static boolean isExperimentalMovieUpsellRemoval() {
        return getBoolean("experimental_movie_upsell", false);
    }

    public static boolean isExperimentalCompactBannerRemoval() {
        return getBoolean("experimental_compact_banner", false);
    }

    public static boolean isExperimentalPaidContentRemoval() {
        return getBoolean("experimental_paid_content", true);
    }

    public static boolean isExperimentalCommentsRemoval() {
        return getBoolean("experimental_comments", false);
    }

    public static boolean isExperimentalCompactMovieRemoval() {
        return getBoolean("experimental_compact_movie", false);
    }

    public static boolean isExperimentalHorizontalMovieShelfRemoval() {
        return getBoolean("experimental_horizontal_movie_shelf", false);
    }

    public static boolean isInFeedSurvey() {
        return getBoolean("experimental_in_feed_survey", false);
    }

    public static boolean isShortsShelf() {
        return getBoolean("experimental_shorts_shelf", true);
    }

    public static boolean isCommunityGuidelines() {
        return getBoolean("experimental_community_guidelines", true);
    }

    public static boolean containsAd(String value, ByteBuffer buffer) {
        try {
            if (!(isExperimentalAdRemoval() || isExperimentalMerchandiseRemoval() || isExperimentalPaidContentRemoval() || isExperimentalCommunityPostRemoval() || isExperimentalMovieUpsellRemoval() || isExperimentalCompactBannerRemoval() || isExperimentalCommentsRemoval() || isExperimentalCompactMovieRemoval() || isExperimentalHorizontalMovieShelfRemoval() || isInFeedSurvey() || isShortsShelf() || isCommunityGuidelines()) || value == null || value.isEmpty()) {
                return false;
            }
            List<String> blockList = new ArrayList<>();
            if (isExperimentalAdRemoval()) {
                blockList.add("_ad");
                blockList.add("ad_badge");
                blockList.add("ads_video_with_context");
                blockList.add("text_search_ad_with_description_first");
                blockList.add("shelf_header");
                blockList.add("cell_divider");
                blockList.add("watch_metadata_app_promo");
            }
            if (isExperimentalMerchandiseRemoval()) {
                blockList.add("product_carousel");
            }
            if (isExperimentalCommunityPostRemoval()) {
                blockList.add("post_base_wrapper");
            }
            if (isExperimentalMovieUpsellRemoval()) {
                blockList.add("movie_and_show_upsell_card");
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
            }
            if (isExperimentalCompactBannerRemoval()) {
                blockList.add("compact_banner");
            }
            if (isExperimentalCommentsRemoval()) {
                blockList.add("comments_composite_entry_point");
            }
            if (isExperimentalCompactMovieRemoval()) {
                blockList.add("compact_movie");
            }
            if (isExperimentalHorizontalMovieShelfRemoval()) {
                blockList.add("horizontal_movie_shelf");
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
            if (!value.contains("related_video_with_context") || indexOf(buffer.array(), endRelatedPageAd) <= 0) {
                for (String s : blockList) {
                    if (value.contains(s)) {
                        if (XGlobals.debug) {
                            Log.d("TemplateBlocked", value);
                        }
                        return true;
                    }
                }
                if (!XGlobals.debug) {
                    return false;
                }
                if (value.contains("related_video_with_context")) {
                    Log.d("Template", value + " | " + bytesToHex(buffer.array()));
                    return false;
                }
                Log.d("Template", value);
                return false;
            }
            if (XGlobals.debug) {
                Log.d("TemplateBlocked", value);
            }
            return true;
        } catch (Exception ex) {
            Log.e("Template", ex.getMessage(), ex);
            return false;
        }
    }

    public static int indexOf(byte[] array, byte[] target) {
        if (target.length == 0) {
            return 0;
        }

        for (int i = 0; i < array.length - target.length + 1; i++) {
            boolean targetFound = true;
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    targetFound = false;
                    break;
                }
            }
            if (targetFound) {
                return i;
            }
        }
        return -1;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            builder.append(String.format("%02x", b));
        return builder.toString();
    }
}
