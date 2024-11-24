package app.revanced.extension.youtube.patches.components;

import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.StringTrieSearch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.NavigationBar;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class LayoutComponentsFilter extends Filter {
    private static final String COMPACT_CHANNEL_BAR_PATH_PREFIX = "compact_channel_bar.eml";
    private static final String VIDEO_ACTION_BAR_PATH_PREFIX = "video_action_bar.eml";
    private static final String ANIMATED_VECTOR_TYPE_PATH = "AnimatedVectorType";

    private static final StringTrieSearch mixPlaylistsExceptions = new StringTrieSearch(
            "V.ED", // Playlist browse id.
            "java.lang.ref.WeakReference"
    );
    private static final ByteArrayFilterGroup mixPlaylistsExceptions2 = new ByteArrayFilterGroup(
            null,
            "cell_description_body"
    );
    private static final ByteArrayFilterGroup mixPlaylists = new ByteArrayFilterGroup(
            null,
            "&list="
    );

    private final StringTrieSearch exceptions = new StringTrieSearch();
    private final StringFilterGroup searchResultShelfHeader;
    private final StringFilterGroup inFeedSurvey;
    private final StringFilterGroup notifyMe;
    private final StringFilterGroup expandableMetadata;
    private final ByteArrayFilterGroup searchResultRecommendations;
    private final StringFilterGroup searchResultVideo;
    private final StringFilterGroup compactChannelBarInner;
    private final StringFilterGroup compactChannelBarInnerButton;
    private final ByteArrayFilterGroup joinMembershipButton;
    private final StringFilterGroup likeSubscribeGlow;
    private final StringFilterGroup horizontalShelves;

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

        final var chipsShelf = new StringFilterGroup(
                Settings.HIDE_CHIPS_SHELF,
                "chips_shelf"
        );

        addIdentifierCallbacks(
                chipsShelf
        );

        // Paths.

        final var communityPosts = new StringFilterGroup(
                Settings.HIDE_COMMUNITY_POSTS,
                "post_base_wrapper",
                "text_post_root.eml",
                "images_post_root.eml",
                "images_post_slim.eml",
                "images_post_root_slim.eml",
                "text_post_root_slim.eml",
                "post_base_wrapper_slim.eml"
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

        final var paidPromotion = new StringFilterGroup(
                Settings.HIDE_PAID_PROMOTION_LABEL,
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
        // Previously this was a setting to show/hide the player button.
        // But it was decided it's simpler to always hide this button because:
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

        final var channelBar = new StringFilterGroup(
                Settings.HIDE_CHANNEL_BAR,
                "channel_bar"
        );

        final var relatedVideos = new StringFilterGroup(
                Settings.HIDE_RELATED_VIDEOS,
                "fullscreen_related_videos"
        );

        final var playables = new StringFilterGroup(
                Settings.HIDE_PLAYABLES,
                "horizontal_gaming_shelf.eml",
                "mini_game_card.eml"
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

        compactChannelBarInner = new StringFilterGroup(
                Settings.HIDE_JOIN_MEMBERSHIP_BUTTON,
                "compact_channel_bar_inner"
        );

        compactChannelBarInnerButton = new StringFilterGroup(
                null,
                "|button.eml|"
        );

        joinMembershipButton = new ByteArrayFilterGroup(
                null,
                "sponsorships"
        );

        likeSubscribeGlow = new StringFilterGroup(
                Settings.DISABLE_LIKE_SUBSCRIBE_GLOW,
                "animated_button_border.eml"
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

        horizontalShelves = new StringFilterGroup(
                Settings.HIDE_HORIZONTAL_SHELVES,
                "horizontal_video_shelf.eml",
                "horizontal_shelf.eml",
                "horizontal_shelf_inline.eml",
                "horizontal_tile_shelf.eml"
        );

        addPathCallbacks(
                expandableMetadata,
                inFeedSurvey,
                notifyMe,
                likeSubscribeGlow,
                channelBar,
                communityPosts,
                paidPromotion,
                searchResultVideo,
                latestPosts,
                channelWatermark,
                communityGuidelines,
                playables,
                quickActions,
                relatedVideos,
                compactBanner,
                compactChannelBarInner,
                medicalPanel,
                infoPanel,
                emergencyBox,
                subscribersCommunityGuidelines,
                channelGuidelines,
                audioTrackButton,
                artistCard,
                timedReactions,
                imageShelf,
                channelMemberShelf,
                forYouShelf,
                horizontalShelves
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == searchResultVideo) {
            if (searchResultRecommendations.check(protobufBufferArray).isFiltered()) {
                return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
            }
            return false;
        }

        if (matchedGroup == likeSubscribeGlow) {
            if ((path.startsWith(VIDEO_ACTION_BAR_PATH_PREFIX) || path.startsWith(COMPACT_CHANNEL_BAR_PATH_PREFIX))
                    && path.contains(ANIMATED_VECTOR_TYPE_PATH)) {
                return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
            }

            return false;
        }

        // The groups are excluded from the filter due to the exceptions list below.
        // Filter them separately here.
        if (matchedGroup == notifyMe || matchedGroup == inFeedSurvey || matchedGroup == expandableMetadata)
        {
            return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
        }

        if (exceptions.matches(path)) return false; // Exceptions are not filtered.

        if (matchedGroup == compactChannelBarInner) {
            if (compactChannelBarInnerButton.check(path).isFiltered()) {
                // The filter may be broad, but in the context of a compactChannelBarInnerButton,
                // it's safe to assume that the button is the only thing that should be hidden.
                if (joinMembershipButton.check(protobufBufferArray).isFiltered()) {
                    return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
                }
            }

            return false;
        }

        // TODO: This also hides the feed Shorts shelf header
        if (matchedGroup == searchResultShelfHeader && contentIndex != 0) return false;

        if (matchedGroup == horizontalShelves) {
            if (contentIndex == 0 && hideShelves()) {
                return super.isFiltered(path, identifier, protobufBufferArray, matchedGroup, contentType, contentIndex);
            }

            return false;
        }

        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }

    /**
     * Injection point.
     * Called from a different place then the other filters.
     */
    public static boolean filterMixPlaylists(final Object conversionContext, @Nullable final byte[] bytes) {
        try {
            if (!Settings.HIDE_MIX_PLAYLISTS.get()) {
                return false;
            }

            if (bytes == null) {
                Logger.printDebug(() -> "bytes is null");
                return false;
            }

            // Prevent playlist items being hidden, if a mix playlist is present in it.
            if (mixPlaylistsExceptions.matches(conversionContext.toString())) {
                return false;
            }

            // Prevent hiding the description of some videos accidentally.
            if (mixPlaylistsExceptions2.check(bytes).isFiltered()) {
                return false;
            }

            if (mixPlaylists.check(bytes).isFiltered()) {
                Logger.printDebug(() -> "Filtered mix playlist");
                return true;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "filterMixPlaylists failure", ex);
        }

        return false;
    }

    /**
     * Injection point.
     */
    public static boolean showWatermark() {
        return !Settings.HIDE_VIDEO_CHANNEL_WATERMARK.get();
    }

    /**
     * Injection point.
     */
    public static void hideAlbumCard(View view) {
        Utils.hideViewBy0dpUnderCondition(Settings.HIDE_ALBUM_CARDS, view);
    }

    /**
     * Injection point.
     */
    public static void hideCrowdfundingBox(View view) {
        Utils.hideViewBy0dpUnderCondition(Settings.HIDE_CROWDFUNDING_BOX, view);
    }

    /**
     * Injection point.
     */
    public static boolean hideFloatingMicrophoneButton(final boolean original) {
        return original || Settings.HIDE_FLOATING_MICROPHONE_BUTTON.get();
    }

    /**
     * Injection point.
     */
    public static int hideInFeed(final int height) {
        return Settings.HIDE_FILTER_BAR_FEED_IN_FEED.get()
                ? 0
                : height;
    }

    /**
     * Injection point.
     */
    public static int hideInSearch(int height) {
        return Settings.HIDE_FILTER_BAR_FEED_IN_SEARCH.get()
                ? 0
                : height;
    }

    /**
     * Injection point.
     */
    public static void hideInRelatedVideos(View chipView) {
        Utils.hideViewBy0dpUnderCondition(Settings.HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS, chipView);
    }

    private static final boolean HIDE_DOODLES_ENABLED = Settings.HIDE_DOODLES.get();

    /**
     * Injection point.
     */
    @Nullable
    public static Drawable hideYoodles(Drawable animatedYoodle) {
        if (HIDE_DOODLES_ENABLED) {
            return null;
        }

        return animatedYoodle;
    }

    private static final boolean HIDE_SHOW_MORE_BUTTON_ENABLED = Settings.HIDE_SHOW_MORE_BUTTON.get();

    /**
     * Injection point.
     */
    public static void hideShowMoreButton(View view) {
        if (HIDE_SHOW_MORE_BUTTON_ENABLED
                && NavigationBar.isSearchBarActive()
                // Search bar can be active but behind the player.
                && !PlayerType.getCurrent().isMaximizedOrFullscreen()) {
            Utils.hideViewByLayoutParams(view);
        }
    }

    private static boolean hideShelves() {
        // If the player is opened while library is selected,
        // then filter any recommendations below the player.
        if (PlayerType.getCurrent().isMaximizedOrFullscreen()
                // Or if the search is active while library is selected, then also filter.
                || NavigationBar.isSearchBarActive()) {
            return true;
        }

        // Check navigation button last.
        // Only filter if the library tab is not selected.
        // This check is important as the shelf layout is used for the library tab playlists.
        return NavigationButton.getSelectedNavigationButton() != NavigationButton.LIBRARY;
    }
}
