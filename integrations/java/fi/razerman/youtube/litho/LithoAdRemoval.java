package fi.razerman.youtube.litho;

import android.util.Log;
import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;
import fi.razerman.youtube.Helpers.SharedPrefs;
import fi.razerman.youtube.XGlobals;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/* loaded from: classes6.dex */
public class LithoAdRemoval {
    private static final byte[] endRelatedPageAd = {112, 97, 103, 101, 97, 100};
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static boolean isExperimentalAdRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_ad_removal", true);
    }

    public static boolean isExperimentalMerchandiseRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_merchandise", false);
    }

    public static boolean isExperimentalCommunityPostRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_community_posts", false);
    }

    public static boolean isExperimentalMovieUpsellRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_movie_upsell", false);
    }

    public static boolean isExperimentalCompactBannerRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_compact_banner", false);
    }

    public static boolean isExperimentalCommentsRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_comments", false);
    }

    public static boolean isExperimentalCompactMovieRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_compact_movie", false);
    }

    public static boolean isExperimentalHorizontalMovieShelfRemoval() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_horizontal_movie_shelf", false);
    }

    public static boolean isInFeedSurvey() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_in_feed_survey", false);
    }

    public static boolean isShortsShelf() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_shorts_shelf", false);
    }

    public static boolean isCommunityGuidelines() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "experimental_community_guidelines", false);
    }

    public static boolean containsAd(String value) {
        if (!(isExperimentalAdRemoval() || isExperimentalMerchandiseRemoval() || isExperimentalCommunityPostRemoval() || isExperimentalMovieUpsellRemoval() || isExperimentalCompactBannerRemoval() || isExperimentalCommentsRemoval() || isExperimentalCompactMovieRemoval() || isExperimentalHorizontalMovieShelfRemoval() || isInFeedSurvey() || isShortsShelf() || isCommunityGuidelines()) || value == null || value.isEmpty()) {
            return false;
        }
        List<String> blockList = new ArrayList<>();
        if (isExperimentalAdRemoval()) {
            blockList.add("_ad");
            blockList.add("ad_badge");
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
        Log.d("Template", value);
        return false;
    }

    public static boolean containsAd(String value, ByteBuffer buffer) {
        try {
            if (!(isExperimentalAdRemoval() || isExperimentalMerchandiseRemoval() || isExperimentalCommunityPostRemoval() || isExperimentalMovieUpsellRemoval() || isExperimentalCompactBannerRemoval() || isExperimentalCommentsRemoval() || isExperimentalCompactMovieRemoval() || isExperimentalHorizontalMovieShelfRemoval() || isInFeedSurvey() || isShortsShelf() || isCommunityGuidelines()) || value == null || value.isEmpty()) {
                return false;
            }
            List<String> blockList = new ArrayList<>();
            if (isExperimentalAdRemoval()) {
                blockList.add("_ad");
                blockList.add("ad_badge");
                blockList.add("ads_video_with_context");
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
                if (array[i+j] != target[j]) {
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
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = hexArray[v & 15];
        }
        return new String(hexChars);
    }
}
