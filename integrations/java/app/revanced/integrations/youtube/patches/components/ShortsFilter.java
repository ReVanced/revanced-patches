package app.revanced.integrations.youtube.patches.components;

import static app.revanced.integrations.shared.Utils.hideViewUnderCondition;
import static app.revanced.integrations.youtube.shared.NavigationBar.NavigationButton;

import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.NavigationBar;
import app.revanced.integrations.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class ShortsFilter extends Filter {
    public static PivotBar pivotBar; // Set by patch.
    private final String REEL_CHANNEL_BAR_PATH = "reel_channel_bar.eml";

    private final StringFilterGroup shortsCompactFeedVideoPath;
    private final ByteArrayFilterGroup shortsCompactFeedVideoBuffer;

    private final StringFilterGroup channelBar;
    private final StringFilterGroup fullVideoLinkLabel;
    private final StringFilterGroup videoTitle;
    private final StringFilterGroup reelSoundMetadata;
    private final StringFilterGroup subscribeButton;
    private final StringFilterGroup subscribeButtonPaused;
    private final StringFilterGroup soundButton;
    private final StringFilterGroup infoPanel;
    private final StringFilterGroup joinButton;
    private final StringFilterGroup shelfHeader;

    private final StringFilterGroup suggestedActionPath;
    private final ByteArrayFilterGroupList suggestedActions =  new ByteArrayFilterGroupList();

    private final StringFilterGroup actionBar;
    private final ByteArrayFilterGroupList videoActionButtonGroupList = new ByteArrayFilterGroupList();

    public ShortsFilter() {
        //
        // Identifier components.
        //

        var shorts = new StringFilterGroup(
                null, // Setting is based on navigation state.
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell",
                "shorts_pivot_item"
        );
        // Feed Shorts shelf header.
        // Use a different filter group for this pattern, as it requires an additional check after matching.
        shelfHeader = new StringFilterGroup(
                null,
                "shelf_header.eml"
        );

        // Home / subscription feed components.

        var thanksButton = new StringFilterGroup( // Edit: Does this item show up anymore?
                Settings.HIDE_SHORTS_THANKS_BUTTON,
                "suggested_action"
        );

        addIdentifierCallbacks(shorts, shelfHeader, thanksButton);

        //
        // Path components.
        //

        // Shorts that appear in the feed/search when the device is using tablet layout.
        shortsCompactFeedVideoPath = new StringFilterGroup(null, "compact_video.eml");
        // Filter out items that use the 'frame0' thumbnail.
        // This is a valid thumbnail for both regular videos and Shorts,
        // but it appears these thumbnails are used only for Shorts.
        shortsCompactFeedVideoBuffer = new ByteArrayFilterGroup(null, "/frame0.jpg");

        // Shorts player components.
        joinButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_JOIN_BUTTON,
                "sponsor_button"
        );

        subscribeButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_SUBSCRIBE_BUTTON,
                "subscribe_button"
        );

        subscribeButtonPaused = new StringFilterGroup(
                Settings.HIDE_SHORTS_SUBSCRIBE_BUTTON_PAUSED,
                "shorts_paused_state"
        );

        channelBar = new StringFilterGroup(
                Settings.HIDE_SHORTS_CHANNEL_BAR,
                REEL_CHANNEL_BAR_PATH
        );

        fullVideoLinkLabel = new StringFilterGroup(
                Settings.HIDE_SHORTS_FULL_VIDEO_LINK_LABEL,
                "reel_multi_format_link"
        );

        videoTitle = new StringFilterGroup(
                Settings.HIDE_SHORTS_VIDEO_TITLE,
                "shorts_video_title_item"
        );

        reelSoundMetadata = new StringFilterGroup(
                Settings.HIDE_SHORTS_SOUND_METADATA_LABEL,
                "reel_sound_metadata"
        );

        soundButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_SOUND_BUTTON,
                "reel_pivot_button"
        );

        infoPanel = new StringFilterGroup(
                Settings.HIDE_SHORTS_INFO_PANEL,
                "shorts_info_panel_overview"
        );

        actionBar = new StringFilterGroup(
                null,
                "shorts_action_bar"
        );

        suggestedActionPath = new StringFilterGroup(
                null,
                "suggested_action.eml"
        );

        addPathCallbacks(
                shortsCompactFeedVideoPath,
                joinButton, subscribeButton, subscribeButtonPaused, suggestedActionPath,
                channelBar, fullVideoLinkLabel, videoTitle, reelSoundMetadata,
                soundButton, infoPanel, actionBar
        );

        //
        // Action buttons
        //
        var shortsLikeButton = new ByteArrayFilterGroup(
                Settings.HIDE_SHORTS_LIKE_BUTTON,
                "shorts_like_button"
        );

        var shortsDislikeButton = new ByteArrayFilterGroup(
                Settings.HIDE_SHORTS_DISLIKE_BUTTON,
                "shorts_dislike_button"
        );

        var shortsCommentButton = new ByteArrayFilterGroup(
                Settings.HIDE_SHORTS_COMMENTS_BUTTON,
                "reel_comment_button"
        );

        var shortsShareButton = new ByteArrayFilterGroup(
                Settings.HIDE_SHORTS_SHARE_BUTTON,
                "reel_share_button"
        );

        var shortsRemixButton = new ByteArrayFilterGroup(
                Settings.HIDE_SHORTS_REMIX_BUTTON,
                "reel_remix_button"
        );

        videoActionButtonGroupList.addAll(
                shortsLikeButton,
                shortsDislikeButton,
                shortsCommentButton,
                shortsShareButton,
                shortsRemixButton
        );

        //
        // Suggested actions.
        //
        suggestedActions.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SHOP_BUTTON,
                        "yt_outline_bag_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_LOCATION_BUTTON,
                        "yt_outline_location_point_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SAVE_SOUND_BUTTON,
                        "yt_outline_list_add_"
                )
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentType == FilterContentType.PATH) {
            // Always filter if matched.
            if (matchedGroup == soundButton ||
                    matchedGroup == infoPanel ||
                    matchedGroup == channelBar ||
                    matchedGroup == fullVideoLinkLabel ||
                    matchedGroup == videoTitle ||
                    matchedGroup == reelSoundMetadata ||
                    matchedGroup == subscribeButtonPaused
            ) return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);

            if (matchedGroup == shortsCompactFeedVideoPath) {
                if (shouldHideShortsFeedItems() && contentIndex == 0
                        && shortsCompactFeedVideoBuffer.check(protobufBufferArray).isFiltered()) {
                    return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
                }
                return false;
            }

            if (matchedGroup == subscribeButton || matchedGroup == joinButton) {
                // Filter only when reelChannelBar is visible to avoid false positives.
                if (path.startsWith(REEL_CHANNEL_BAR_PATH)) return super.isFiltered(
                        identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex
                );
                return false;
            }

            // Video action buttons (like, dislike, comment, share, remix) have the same path.
            if (matchedGroup == actionBar) {
                if (videoActionButtonGroupList.check(protobufBufferArray).isFiltered()) return super.isFiltered(
                        identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex
                );
                return false;
            }

            if (matchedGroup == suggestedActionPath) {
                if (contentIndex == 0 && suggestedActions.check(protobufBufferArray).isFiltered()) return super.isFiltered(
                        identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex
                );
                // else, return false;
            }

            return false;
        } else {
            // Feed/search path components.
            if (matchedGroup == shelfHeader) {
                // Because the header is used in watch history and possibly other places, check for the index,
                // which is 0 when the shelf header is used for Shorts.
                if (contentIndex != 0) return false;
            }

            if (!shouldHideShortsFeedItems()) return false;
        }

        // Super class handles logging.
        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }

    private static boolean shouldHideShortsFeedItems() {
        final boolean hideHome = Settings.HIDE_SHORTS_HOME.get();
        final boolean hideSubscriptions = Settings.HIDE_SHORTS_SUBSCRIPTIONS.get();
        final boolean hideSearch = Settings.HIDE_SHORTS_SEARCH.get();

        if (hideHome && hideSubscriptions && hideSearch) {
            // Shorts suggestions can load in the background if a video is opened and
            // then immediately minimized before any suggestions are loaded.
            // In this state the player type will show minimized, which makes it not possible to
            // distinguish between Shorts suggestions loading in the player and between
            // scrolling thru search/home/subscription tabs while a player is minimized.
            //
            // To avoid this situation for users that never want to show Shorts (all hide Shorts options are enabled)
            // then hide all Shorts everywhere including the Library history and Library playlists.
            return true;
        }

        // Must check player type first, as search bar can be active behind the player.
        if (PlayerType.getCurrent().isMaximizedOrFullscreen()) {
            // For now, consider the under video results the same as the home feed.
            return hideHome;
        }

        // Must check second, as search can be from any tab.
        if (NavigationBar.isSearchBarActive()) {
            return hideSearch;
        }

        // Avoid checking navigation button status if all other Shorts should show.
        if (!hideHome && !hideSubscriptions) {
            return false;
        }

        NavigationButton selectedNavButton = NavigationButton.getSelectedNavigationButton();
        if (selectedNavButton == null) {
            return hideHome; // Unknown tab, treat the same as home.
        }
        if (selectedNavButton == NavigationButton.HOME) {
            return hideHome;
        }
        if (selectedNavButton == NavigationButton.SUBSCRIPTIONS) {
            return hideSubscriptions;
        }
        // User must be in the library tab.  Don't hide the history or any playlists here.
        return false;
    }

    public static void hideShortsShelf(final View shortsShelfView) {
        if (shouldHideShortsFeedItems()) {
            Utils.hideViewByLayoutParams(shortsShelfView);
        }
    }

    // region Hide the buttons in older versions of YouTube. New versions use Litho.

    public static void hideShortsCommentsButton(final View commentsButtonView) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_COMMENTS_BUTTON, commentsButtonView);
    }

    public static void hideShortsRemixButton(final View remixButtonView) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_REMIX_BUTTON, remixButtonView);
    }

    public static void hideShortsShareButton(final View shareButtonView) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_SHARE_BUTTON, shareButtonView);
    }

    // endregion

    public static void hideNavigationBar() {
        if (!Settings.HIDE_SHORTS_NAVIGATION_BAR.get()) return;
        if (pivotBar == null) return;

        pivotBar.setVisibility(View.GONE);
    }

    public static View hideNavigationBar(final View navigationBarView) {
        if (Settings.HIDE_SHORTS_NAVIGATION_BAR.get())
            return null; // Hides the navigation bar.

        return navigationBarView;
    }
}
