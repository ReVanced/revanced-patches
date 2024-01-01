package app.revanced.integrations.youtube.patches.components;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.StringTrieSearch;

@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.N)
public final class LayoutComponentsFilter extends Filter {
    private final StringTrieSearch exceptions = new StringTrieSearch();
    private static final StringTrieSearch mixPlaylistsExceptions = new StringTrieSearch();
    private static final ByteArrayFilterGroup mixPlaylistsExceptions2 = new ByteArrayFilterGroup(
            null,
            "cell_description_body"
    );
    private final CustomFilterGroup custom;

    private static final ByteArrayFilterGroup mixPlaylists = new ByteArrayFilterGroup(
            Settings.HIDE_MIX_PLAYLISTS,
            "&list="
    );
    private final StringFilterGroup searchResultShelfHeader;
    private final StringFilterGroup inFeedSurvey;
    private final StringFilterGroup notifyMe;
    private final StringFilterGroup expandableMetadata;
    private final ByteArrayFilterGroup searchResultRecommendations;
    private final StringFilterGroup searchResultVideo;

    static {
        mixPlaylistsExceptions.addPatterns(
                "V.ED", // Playlist browse id.
                "java.lang.ref.WeakReference"
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LayoutComponentsFilter() {
        exceptions.addPatterns(
                "home_video_with_context",
                "related_video_with_context",
                "search_video_with_context",
                "comment_thread", // Whitelist comments
                "|comment.", // Whitelist comment replies
                "library_recent_shelf"
        );

        // Identifiers.

        final var graySeparator = new StringFilterGroup(
                Settings.HIDE_GRAY_SEPARATOR,
                "cell_divider" // layout residue (gray line above the buttoned ad),
        );

        final var chipsShelf = new StringFilterGroup(
                Settings.HIDE_CHIPS_SHELF,
                "chips_shelf"
        );

        addIdentifierCallbacks(
                graySeparator,
                chipsShelf
        );

        // Paths.

        custom = new CustomFilterGroup(
                Settings.CUSTOM_FILTER,
                Settings.CUSTOM_FILTER_STRINGS
        );

        final var communityPosts = new StringFilterGroup(
                Settings.HIDE_COMMUNITY_POSTS,
                "post_base_wrapper"
        );

        final var communityGuidelines = new StringFilterGroup(
                Settings.HIDE_COMMUNITY_GUIDELINES,
                "community_guidelines"
        );

        final var subscribersCommunityGuidelines = new StringFilterGroup(
                Settings.HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES,
                "sponsorships_comments_upsell"
        );

        final var channelMemberShelf = new StringFilterGroup(
                Settings.HIDE_CHANNEL_MEMBER_SHELF,
                "member_recognition_shelf"
        );

        final var compactBanner = new StringFilterGroup(
                Settings.HIDE_COMPACT_BANNER,
                "compact_banner"
        );

        inFeedSurvey = new StringFilterGroup(
                Settings.HIDE_FEED_SURVEY,
                "in_feed_survey",
                "slimline_survey"
        );

        final var medicalPanel = new StringFilterGroup(
                Settings.HIDE_MEDICAL_PANELS,
                "medical_panel"
        );

        final var paidContent = new StringFilterGroup(
                Settings.HIDE_PAID_CONTENT,
                "paid_content_overlay"
        );

        final var infoPanel = new StringFilterGroup(
                Settings.HIDE_HIDE_INFO_PANELS,
                "publisher_transparency_panel",
                "single_item_information_panel"
        );

        final var latestPosts = new StringFilterGroup(
                Settings.HIDE_HIDE_LATEST_POSTS,
                "post_shelf"
        );

        final var channelGuidelines = new StringFilterGroup(
                Settings.HIDE_HIDE_CHANNEL_GUIDELINES,
                "channel_guidelines_entry_banner"
        );

        final var emergencyBox = new StringFilterGroup(
                Settings.HIDE_EMERGENCY_BOX,
                "emergency_onebox"
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
                Settings.HIDE_ARTIST_CARDS,
                "official_card"
        );

        expandableMetadata = new StringFilterGroup(
                Settings.HIDE_EXPANDABLE_CHIP,
                "inline_expander"
        );

        final var videoQualityMenuFooter = new StringFilterGroup(
                Settings.HIDE_VIDEO_QUALITY_MENU_FOOTER,
                "quality_sheet_footer"
        );

        final var channelBar = new StringFilterGroup(
                Settings.HIDE_CHANNEL_BAR,
                "channel_bar"
        );

        final var relatedVideos = new StringFilterGroup(
                Settings.HIDE_RELATED_VIDEOS,
                "fullscreen_related_videos"
        );

        final var quickActions = new StringFilterGroup(
                Settings.HIDE_QUICK_ACTIONS,
                "quick_actions"
        );

        final var imageShelf = new StringFilterGroup(
                Settings.HIDE_IMAGE_SHELF,
                "image_shelf"
        );


        final var timedReactions = new StringFilterGroup(
                Settings.HIDE_TIMED_REACTIONS,
                "emoji_control_panel",
                "timed_reaction"
        );

        searchResultShelfHeader = new StringFilterGroup(
                Settings.HIDE_SEARCH_RESULT_SHELF_HEADER,
                "shelf_header.eml"
        );

        notifyMe = new StringFilterGroup(
                Settings.HIDE_NOTIFY_ME_BUTTON,
                "set_reminder_button"
        );

        final var joinMembership = new StringFilterGroup(
                Settings.HIDE_JOIN_MEMBERSHIP_BUTTON,
                "compact_sponsor_button"
        );

        final var channelWatermark = new StringFilterGroup(
                Settings.HIDE_VIDEO_CHANNEL_WATERMARK,
                "featured_channel_watermark_overlay"
        );

        final var forYouShelf = new StringFilterGroup(
                Settings.HIDE_FOR_YOU_SHELF,
                "mixed_content_shelf"
        );

        searchResultVideo = new StringFilterGroup(
                Settings.HIDE_SEARCH_RESULT_RECOMMENDATIONS,
                "search_video_with_context.eml"
        );

        searchResultRecommendations = new ByteArrayFilterGroup(
                Settings.HIDE_SEARCH_RESULT_RECOMMENDATIONS,
                "endorsement_header_footer"
        );

        addPathCallbacks(
                custom,
                expandableMetadata,
                inFeedSurvey,
                notifyMe,
                channelBar,
                communityPosts,
                paidContent,
                searchResultVideo,
                latestPosts,
                channelWatermark,
                communityGuidelines,
                quickActions,
                relatedVideos,
                compactBanner,
                joinMembership,
                medicalPanel,
                videoQualityMenuFooter,
                infoPanel,
                emergencyBox,
                subscribersCommunityGuidelines,
                channelGuidelines,
                audioTrackButton,
                artistCard,
                timedReactions,
                imageShelf,
                channelMemberShelf,
                forYouShelf
        );
    }

    @Override
    public boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == searchResultVideo) {
            if (searchResultRecommendations.check(protobufBufferArray).isFiltered()) {
                return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
            }
        }

        // The groups are excluded from the filter due to the exceptions list below.
        // Filter them separately here.
        if (matchedGroup == notifyMe || matchedGroup == inFeedSurvey || matchedGroup == expandableMetadata)
            return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);

        if (matchedGroup != custom && exceptions.matches(path))
            return false; // Exceptions are not filtered.

        // TODO: This also hides the feed Shorts shelf header
        if (matchedGroup == searchResultShelfHeader && contentIndex != 0) return false;

        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }

    /**
     * Injection point.
     * Called from a different place then the other filters.
     */
    public static boolean filterMixPlaylists(final Object conversionContext, @Nullable final byte[] bytes) {
        if (bytes == null) {
            Logger.printDebug(() -> "bytes is null");
            return false;
        }

        // Prevent playlist items being hidden, if a mix playlist is present in it.
        if (mixPlaylistsExceptions.matches(conversionContext.toString()))
            return false;

        if (!mixPlaylists.check(bytes).isFiltered())
            return false;

        // Prevent hiding the description of some videos accidentally.
        if (mixPlaylistsExceptions2.check(bytes).isFiltered())
            return false;

        Logger.printDebug(() -> "Filtered mix playlist");
        return true;
    }

    public static boolean showWatermark() {
        return !Settings.HIDE_VIDEO_CHANNEL_WATERMARK.get();
    }
}
