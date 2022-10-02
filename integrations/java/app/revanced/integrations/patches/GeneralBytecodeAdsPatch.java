package app.revanced.integrations.patches;

import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class GeneralBytecodeAdsPatch {
    //Used by app.revanced.patches.youtube.ad.general.bytecode.patch.GeneralBytecodeAdsPatch
    public static boolean isAdComponent(StringBuilder pathBuilder, String identifier) {
        var path = pathBuilder.toString();
        if (path.isEmpty()) return false;

        LogHelper.debug(GeneralBytecodeAdsPatch.class, String.format("Searching (ID: %s): %s", identifier, path));

        if (containsAny(path,
                "home_video_with_context",
                "related_video_with_context",
                "search_video_with_context",
                "menu",
                "root",
                "-count",
                "-space",
                "-button",
                "library_recent_shelf",
                "download_button"
        )) return false;

        List<String> blockList = new ArrayList<>();

        for (var ad : SettingsEnum.ADREMOVER_CUSTOM.getString().split(",")) {
            if (ad.isEmpty()) continue;
            blockList.add(ad);
        }

        if (SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL.getBoolean()) {
            if (identifier != null && identifier.contains("carousel_ad")) {
                LogHelper.debug(GeneralBytecodeAdsPatch.class, "Blocking: " + identifier);
                return true;
            }

            blockList.add("video_display_full_buttoned_layout");
            blockList.add("_ad");
            blockList.add("ad_");
            blockList.add("ads_video_with_context");
            blockList.add("cell_divider");
            blockList.add("reels_player_overlay");
            // could be required
            // blockList.add("full_width_square_image_layout");
            blockList.add("shelf_header");
            blockList.add("watch_metadata_app_promo");
            blockList.add("video_display_full_layout");
        }

        if (SettingsEnum.ADREMOVER_MOVIE_REMOVAL.getBoolean()) {
            blockList.add("browsy_bar");
            blockList.add("compact_movie");
            blockList.add("horizontal_movie_shelf");
            blockList.add("movie_and_show_upsell_card");
        }

        if (SettingsEnum.ADREMOVER_COMMENTS_REMOVAL.getBoolean()) {
            blockList.add("comments_");
        }
        if (SettingsEnum.ADREMOVER_COMMUNITY_GUIDELINES.getBoolean()) {
            blockList.add("community_guidelines");
        }
        if (SettingsEnum.ADREMOVER_COMPACT_BANNER_REMOVAL.getBoolean()) {
            blockList.add("compact_banner");
        }
        if (SettingsEnum.ADREMOVER_EMERGENCY_BOX_REMOVAL.getBoolean()) {
            blockList.add("emergency_onebox");
        }
        if (SettingsEnum.ADREMOVER_FEED_SURVEY_REMOVAL.getBoolean()) {
            blockList.add("in_feed_survey");
        }
        if (SettingsEnum.ADREMOVER_MEDICAL_PANEL_REMOVAL.getBoolean()) {
            blockList.add("medical_panel");
        }
        if (SettingsEnum.ADREMOVER_PAID_CONTECT_REMOVAL.getBoolean()) {
            blockList.add("paid_content_overlay");
        }
        if (SettingsEnum.ADREMOVER_COMMUNITY_POSTS_REMOVAL.getBoolean()) {
            blockList.add("post_base_wrapper");
        }
        if (SettingsEnum.ADREMOVER_MERCHANDISE_REMOVAL.getBoolean()) {
            blockList.add("product_carousel");
        }
        if (SettingsEnum.ADREMOVER_SHORTS_SHELF.getBoolean()) {
            blockList.add("shorts_shelf");
        }
        if (SettingsEnum.ADREMOVER_INFO_PANEL_REMOVAL.getBoolean()) {
            blockList.add("publisher_transparency_panel");
            blockList.add("single_item_information_panel");
        }
        if (SettingsEnum.ADREMOVER_HIDE_SUGGESTIONS.getBoolean()) {
            blockList.add("horizontal_video_shelf");
        }
        if (SettingsEnum.ADREMOVER_HIDE_LATEST_POSTS.getBoolean()) {
            blockList.add("post_shelf");
        }
        if (SettingsEnum.ADREMOVER_HIDE_CHANNEL_GUIDELINES.getBoolean()) {
            blockList.add("channel_guidelines_entry_banner");
        }

        if (anyMatch(blockList, path::contains)) {
            LogHelper.debug(GeneralBytecodeAdsPatch.class, "Blocking: " + path);
            return true;
        }

        return false;
    }

    private static boolean containsAny(String value, String... targets) {
        for (String string : targets)
            if (value.contains(string)) return true;
        return false;
    }

    private static <T> boolean anyMatch(List<T> value, APredicate<? super T> predicate) {
        for (T t : value) {
            if (predicate.test(t)) return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface APredicate<T> {
        boolean test(T t);
    }
}
