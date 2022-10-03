package app.revanced.integrations.patches;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

/**
 * Helper functions.
 */
final class Extensions {
    static boolean containsAny(final String value, final String... targets) {
        for (String string : targets)
            if (value.contains(string)) return true;
        return false;
    }
}

final class ComponentRule {
    private final SettingsEnum setting;
    private final String[] blocks;

    /**
     * Initialize a new rule for components.
     *
     * @param setting The setting which controls the blocking of this component.
     * @param blocks  The rules to block the component on.
     */
    public ComponentRule(final SettingsEnum setting, final String... blocks) {
        this.setting = setting;
        this.blocks = blocks;
    }

    public boolean isEnabled() {
        return setting.getBoolean();
    }

    public boolean isBlocked(final String string) {
        return Extensions.containsAny(string, blocks);
    }
}

final class LithoBlockRegister {
    private final ArrayList<ComponentRule> blocks = new ArrayList<>();

    public void addBlock(final ComponentRule block) {
        blocks.add(block);
    }

    public boolean isBlocked(final String value) {
        for (ComponentRule block : blocks)
            if (block.isEnabled() && block.isBlocked(value)) return true;
        return false;
    }
}

public class GeneralBytecodeAdsPatch {
    private final static LithoBlockRegister pathBlockRegister = new LithoBlockRegister();
    private final static ComponentRule identifierBlock;

    static {
        var comments = new ComponentRule(SettingsEnum.ADREMOVER_COMMENTS_REMOVAL, "comments_");
        var communityPosts = new ComponentRule(SettingsEnum.ADREMOVER_COMMUNITY_POSTS_REMOVAL, "post_base_wrapper");
        var communityGuidelines = new ComponentRule(SettingsEnum.ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL, "community_guidelines");
        var compactBanner = new ComponentRule(SettingsEnum.ADREMOVER_COMPACT_BANNER_REMOVAL, "compact_banner");
        var inFeedSurvey = new ComponentRule(SettingsEnum.ADREMOVER_FEED_SURVEY_REMOVAL, "in_feed_survey");
        var medicalPanel = new ComponentRule(SettingsEnum.ADREMOVER_MEDICAL_PANEL_REMOVAL, "medical_panel");
        var paidContent = new ComponentRule(SettingsEnum.ADREMOVER_PAID_CONTECT_REMOVAL, "paid_content_overlay");
        var merchandise = new ComponentRule(SettingsEnum.ADREMOVER_MERCHANDISE_REMOVAL, "product_carousel");
        var shorts = new ComponentRule(SettingsEnum.ADREMOVER_SHORTS_SHELF_REMOVAL, "shorts_shelf");
        var infoPanel = new ComponentRule(SettingsEnum.ADREMOVER_INFO_PANEL_REMOVAL, "publisher_transparency_panel", "single_item_information_panel");
        var suggestions = new ComponentRule(SettingsEnum.ADREMOVER_SUGGESTIONS_REMOVAL, "horizontal_video_shelf");
        var latestPosts = new ComponentRule(SettingsEnum.ADREMOVER_HIDE_LATEST_POSTS, "post_shelf");
        var channelGuidelines = new ComponentRule(SettingsEnum.ADREMOVER_HIDE_CHANNEL_GUIDELINES, "channel_guidelines_entry_banner");
        var generalAds = new ComponentRule(
                SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL,
                // could be required
                //"full_width_square_image_layout",
                "video_display_full_buttoned_layout",
                "_ad",
                "ad_",
                "ads_video_with_context",
                "cell_divider",
                "reels_player_overlay",
                "shelf_header",
                "watch_metadata_app_promo",
                "video_display_full_layout"
        );
        var movieAds = new ComponentRule(
                SettingsEnum.ADREMOVER_MOVIE_REMOVAL,
                "browsy_bar",
                "compact_movie",
                "horizontal_movie_shelf",
                "movie_and_show_upsell_card"
        );

        // collect and add the blocks
        var blocks = new ComponentRule[]{
                generalAds,
                communityPosts,
                paidContent,
                shorts,
                suggestions,
                latestPosts,
                movieAds,
                comments,
                communityGuidelines,
                compactBanner,
                inFeedSurvey,
                medicalPanel,
                merchandise,
                infoPanel,
                channelGuidelines
        };
        for (var block : blocks) pathBlockRegister.addBlock(block);

        // Block for the ComponentContext.identifier field
        identifierBlock = new ComponentRule(SettingsEnum.ADREMOVER_GENERAL_ADS_REMOVAL, "carousel_ad");
    }

    //Used by app.revanced.patches.youtube.ad.general.bytecode.patch.GeneralBytecodeAdsPatch
    public static boolean isAdComponent(StringBuilder pathBuilder, String identifier) {
        var path = pathBuilder.toString();
        if (path.isEmpty()) return false;

        LogHelper.debug(GeneralBytecodeAdsPatch.class, String.format("Searching (ID: %s): %s", identifier, path));
        // Do not block on these
        if (Extensions.containsAny(path,
                "home_video_with_context",
                "related_video_with_context",
                "search_video_with_context",
                "download_button",
                "library_recent_shelf",
                "menu",
                "root",
                "-count",
                "-space",
                "-button"
        )) return false;

        if (pathBlockRegister.isBlocked(path)) {
            LogHelper.debug(GeneralBytecodeAdsPatch.class, "Blocked: " + path);
            return true;
        }

        if (identifier != null && identifierBlock.isBlocked(identifier)){
            LogHelper.debug(GeneralBytecodeAdsPatch.class, "Blocked: " + identifier);
            return true;
        }

        return false;
    }
}
