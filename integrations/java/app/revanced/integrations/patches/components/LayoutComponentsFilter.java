package app.revanced.integrations.patches.components;


import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.StringTrieSearch;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class LayoutComponentsFilter extends Filter {
    private final StringTrieSearch exceptions = new StringTrieSearch();
    private final CustomFilterGroup custom;

    private static final ByteArrayAsStringFilterGroup mixPlaylists = new ByteArrayAsStringFilterGroup(
            SettingsEnum.HIDE_MIX_PLAYLISTS,
            "&list="
    );
    private final StringFilterGroup searchResultShelfHeader;
    private final StringFilterGroup inFeedSurvey;
    private final StringFilterGroup notifyMe;
    private final StringFilterGroup expandableMetadata;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LayoutComponentsFilter() {
        exceptions.addPatterns(
                "home_video_with_context",
                "related_video_with_context",
                "comment_thread", // Whitelist comments
                "|comment.", // Whitelist comment replies
                "library_recent_shelf"
        );

        custom = new CustomFilterGroup(
                SettingsEnum.CUSTOM_FILTER,
                SettingsEnum.CUSTOM_FILTER_STRINGS
        );

        final var communityPosts = new StringFilterGroup(
                SettingsEnum.HIDE_COMMUNITY_POSTS,
                "post_base_wrapper"
        );

        final var communityGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_COMMUNITY_GUIDELINES,
                "community_guidelines"
        );

        final var subscribersCommunityGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES,
                "sponsorships_comments_upsell"
        );


        final var channelMemberShelf = new StringFilterGroup(
                SettingsEnum.HIDE_CHANNEL_MEMBER_SHELF,
                "member_recognition_shelf"
        );

        final var compactBanner = new StringFilterGroup(
                SettingsEnum.HIDE_COMPACT_BANNER,
                "compact_banner"
        );

        inFeedSurvey = new StringFilterGroup(
                SettingsEnum.HIDE_FEED_SURVEY,
                "in_feed_survey",
                "slimline_survey"
        );

        final var medicalPanel = new StringFilterGroup(
                SettingsEnum.HIDE_MEDICAL_PANELS,
                "medical_panel"
        );

        final var paidContent = new StringFilterGroup(
                SettingsEnum.HIDE_PAID_CONTENT,
                "paid_content_overlay"
        );

        final var infoPanel = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_INFO_PANELS,
                "publisher_transparency_panel",
                "single_item_information_panel"
        );

        final var latestPosts = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_LATEST_POSTS,
                "post_shelf"
        );

        final var channelGuidelines = new StringFilterGroup(
                SettingsEnum.HIDE_HIDE_CHANNEL_GUIDELINES,
                "channel_guidelines_entry_banner"
        );

        // The player audio track button does the exact same function as the audio track flyout menu option.
        // But if the copy url button is shown, these button clashes and the the audio button does not work.
        // Previously this was a setting to show/hide the player button.
        // But it was decided it's simpler to always hide this button because:
        // - it doesn't work with copy video url feature
        // - the button is rare
        // - always hiding makes the ReVanced settings simpler and easier to understand
        // - nobody is going to notice the redundant button is always hidden
        final var audioTrackButton = new StringFilterGroup(
                null,
                "multi_feed_icon_button"
        );

        final var artistCard = new StringFilterGroup(
                SettingsEnum.HIDE_ARTIST_CARDS,
                "official_card"
        );

        expandableMetadata = new StringFilterGroup(
                SettingsEnum.HIDE_EXPANDABLE_CHIP,
                "inline_expander"
        );

        final var chapters = new StringFilterGroup(
                SettingsEnum.HIDE_CHAPTERS,
                "macro_markers_carousel"
        );

        final var channelBar = new StringFilterGroup(
                SettingsEnum.HIDE_CHANNEL_BAR,
                "channel_bar"
        );

        final var relatedVideos = new StringFilterGroup(
                SettingsEnum.HIDE_RELATED_VIDEOS,
                "fullscreen_related_videos"
        );

        final var quickActions = new StringFilterGroup(
                SettingsEnum.HIDE_QUICK_ACTIONS,
                "quick_actions"
        );

        final var imageShelf = new StringFilterGroup(
                SettingsEnum.HIDE_IMAGE_SHELF,
                "image_shelf"
        );

        final var graySeparator = new StringFilterGroup(
                SettingsEnum.HIDE_GRAY_SEPARATOR,
                "cell_divider" // layout residue (gray line above the buttoned ad),
        );

        final var timedReactions = new StringFilterGroup(
                SettingsEnum.HIDE_TIMED_REACTIONS,
                "emoji_control_panel",
                "timed_reaction"
        );

        searchResultShelfHeader = new StringFilterGroup(
                SettingsEnum.HIDE_SEARCH_RESULT_SHELF_HEADER,
                "shelf_header.eml"
        );

        notifyMe = new StringFilterGroup(
                SettingsEnum.HIDE_NOTIFY_ME_BUTTON,
                "set_reminder_button"
        );

        final var joinMembership = new StringFilterGroup(
                SettingsEnum.HIDE_JOIN_MEMBERSHIP_BUTTON,
                "compact_sponsor_button"
        );

        final var chipsShelf = new StringFilterGroup(
                SettingsEnum.HIDE_CHIPS_SHELF,
                "chips_shelf"
        );

        final var channelWatermark = new StringFilterGroup(
                SettingsEnum.HIDE_VIDEO_CHANNEL_WATERMARK,
                "featured_channel_watermark_overlay"
        );

        this.pathFilterGroupList.addAll(
                channelBar,
                communityPosts,
                paidContent,
                latestPosts,
                channelWatermark,
                communityGuidelines,
                quickActions,
                expandableMetadata,
                relatedVideos,
                compactBanner,
                inFeedSurvey,
                joinMembership,
                medicalPanel,
                notifyMe,
                infoPanel,
                subscribersCommunityGuidelines,
                channelGuidelines,
                audioTrackButton,
                artistCard,
                timedReactions,
                imageShelf,
                channelMemberShelf,
                custom
        );

        this.identifierFilterGroupList.addAll(
                graySeparator,
                chipsShelf,
                chapters
        );
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {

        // The groups are excluded from the filter due to the exceptions list below.
        // Filter them separately here.
        if (matchedGroup == notifyMe || matchedGroup == inFeedSurvey || matchedGroup == expandableMetadata) 
            return super.isFiltered(identifier, path, protobufBufferArray, matchedList, matchedGroup, matchedIndex);

        if (matchedGroup != custom && exceptions.matches(path))
            return false; // Exceptions are not filtered.

        // TODO: This also hides the feed Shorts shelf header
        if (matchedGroup == searchResultShelfHeader && matchedIndex != 0) return false;

        return super.isFiltered(identifier, path, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }


    /**
     * Injection point.
     * Called from a different place then the other filters.
     */
    public static boolean filterMixPlaylists(final byte[] bytes) {
        final boolean isMixPlaylistFiltered = mixPlaylists.check(bytes).isFiltered();

        if (isMixPlaylistFiltered)
            LogHelper.printDebug(() -> "Filtered mix playlist");

        return isMixPlaylistFiltered;
    }

    public static boolean showWatermark() {
        return !SettingsEnum.HIDE_VIDEO_CHANNEL_WATERMARK.getBoolean();
    }
}
