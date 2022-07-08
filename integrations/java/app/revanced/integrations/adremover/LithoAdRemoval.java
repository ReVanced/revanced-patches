package app.revanced.integrations.adremover;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class LithoAdRemoval {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean containsAd(String value, ByteBuffer buffer) {
        boolean enabled = false;
        for (SettingsEnum setting : SettingsEnum.getAdRemovalSettings()) {
            if (setting.getBoolean()) {
                enabled = true;
                break;
            }
        }

        try {
            if (value == null || value.isEmpty() || !enabled) return false;
            LogHelper.debug(LithoAdRemoval.class, "Searching for AD: " + value);

            List<String> blockList = new ArrayList<>();
            List<String> bufferBlockList = new ArrayList<>();

            if (SettingsEnum.ADREMOVER_AD_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("_ad");
                blockList.add("ad_badge");
                blockList.add("ads_video_with_context");
                blockList.add("cell_divider");
                blockList.add("reels_player_overlay");
                blockList.add("shelf_header");
                blockList.add("text_search_ad_with_description_first");
                blockList.add("watch_metadata_app_promo");

                bufferBlockList.add("ad_cpn");
            }
            if (SettingsEnum.ADREMOVER_SUGGESTED_FOR_YOU_REMOVAL_BOOLEAN.getBoolean()) {
                bufferBlockList.add("watch-vrecH");
            }
            if (SettingsEnum.ADREMOVER_MOVIE_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("browsy_bar");
                blockList.add("compact_movie");
                blockList.add("horizontal_movie_shelf");
                blockList.add("movie_and_show_upsell_card");

                bufferBlockList.add("YouTube Movies");
            }
            if (containsAny(value, "home_video_with_context", "related_video_with_context") &&
                    bufferBlockList.stream().anyMatch(StandardCharsets.UTF_8.decode(buffer).toString()::contains)
            ) return true;

            if (SettingsEnum.ADREMOVER_COMMENTS_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("comments_");
            }
            if (SettingsEnum.ADREMOVER_COMMUNITY_GUIDELINES_BOOLEAN.getBoolean()) {
                blockList.add("community_guidelines");
            }
            if (SettingsEnum.ADREMOVER_COMPACT_BANNER_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("compact_banner");
            }
            if (SettingsEnum.ADREMOVER_EMERGENCY_BOX_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("emergency_onebox");
            }
            if (SettingsEnum.ADREMOVER_FEED_SURVEY_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("in_feed_survey");
            }
            if (SettingsEnum.ADREMOVER_MEDICAL_PANEL_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("medical_panel");
            }
            if (SettingsEnum.ADREMOVER_PAID_CONTECT_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("paid_content_overlay");
            }
            if (SettingsEnum.ADREMOVER_COMMUNITY_POSTS_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("post_base_wrapper");
            }
            if (SettingsEnum.ADREMOVER_MERCHANDISE_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("product_carousel");
            }
            if (SettingsEnum.ADREMOVER_SHORTS_SHELF_BOOLEAN.getBoolean()) {
                blockList.add("shorts_shelf");
            }
            if (SettingsEnum.ADREMOVER_INFO_PANEL_REMOVAL_BOOLEAN.getBoolean()) {
                blockList.add("publisher_transparency_panel");
                blockList.add("single_item_information_panel");
            }
            if (SettingsEnum.ADREMOVER_HIDE_SUGGESTIONS_BOOLEAN.getBoolean()) {
                blockList.add("horizontal_video_shelf");
            }
            if (SettingsEnum.ADREMOVER_HIDE_LATEST_POSTS_BOOLEAN.getBoolean()) {
                blockList.add("post_shelf");
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
                LogHelper.debug(LithoAdRemoval.class, "Blocking ad: " + value);
                return true;
            }

            if (SettingsEnum.DEBUG_BOOLEAN.getBoolean()) {
                if (value.contains("related_video_with_context")) {
                    LogHelper.debug(LithoAdRemoval.class, value + " | " + bytesToHex(buffer.array()));
                    return false;
                }
                LogHelper.debug(LithoAdRemoval.class, value + " returns false.");
            }
            return false;
        } catch (Exception ex) {
            LogHelper.printException(LithoAdRemoval.class, ex.getMessage(), ex);
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
