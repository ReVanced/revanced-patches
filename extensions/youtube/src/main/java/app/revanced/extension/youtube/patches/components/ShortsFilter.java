package app.revanced.extension.youtube.patches.components;

import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.NavigationBar;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class ShortsFilter extends Filter {
    private static final boolean HIDE_SHORTS_NAVIGATION_BAR = Settings.HIDE_SHORTS_NAVIGATION_BAR.get();
    private static final String REEL_CHANNEL_BAR_PATH = "reel_channel_bar.eml";

    /**
     * For paid promotion label and subscribe button that appears in the channel bar.
     */
    private static final String REEL_METAPANEL_PATH = "reel_metapanel.eml";

    /**
     * Tags that appears when opening the Shorts player.
     */
    private static final List<String> REEL_WATCH_FRAGMENT_INIT_PLAYBACK = Arrays.asList("r_fs", "r_ts");

    /**
     * Vertical padding between the bottom of the screen and the seekbar, when the Shorts navigation bar is hidden.
     */
    public static final int HIDDEN_NAVIGATION_BAR_VERTICAL_HEIGHT = 100;

    private static WeakReference<PivotBar> pivotBarRef = new WeakReference<>(null);

    private final StringFilterGroup shortsCompactFeedVideoPath;
    private final ByteArrayFilterGroup shortsCompactFeedVideoBuffer;

    private final StringFilterGroup subscribeButton;
    private final StringFilterGroup joinButton;
    private final StringFilterGroup paidPromotionButton;
    private final StringFilterGroup shelfHeader;

    private final StringFilterGroup suggestedAction;
    private final ByteArrayFilterGroupList suggestedActionsGroupList = new ByteArrayFilterGroupList();

    private final StringFilterGroup shortsActionBar;
    private final StringFilterGroup actionButton;
    private final ByteArrayFilterGroupList videoActionButtonGroupList = new ByteArrayFilterGroupList();

    public ShortsFilter() {
        //
        // Identifier components.
        //

        var shortsIdentifiers = new StringFilterGroup(
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

        addIdentifierCallbacks(shortsIdentifiers, shelfHeader);

        //
        // Path components.
        //

        shortsCompactFeedVideoPath = new StringFilterGroup(null,
                // Shorts that appear in the feed/search when the device is using tablet layout.
                "compact_video.eml",
                // 'video_lockup_with_attachment.eml' is shown instead of 'compact_video.eml' for some users
                "video_lockup_with_attachment.eml",
                // Search results that appear in a horizontal shelf.
                "video_card.eml");

        // Filter out items that use the 'frame0' thumbnail.
        // This is a valid thumbnail for both regular videos and Shorts,
        // but it appears these thumbnails are used only for Shorts.
        shortsCompactFeedVideoBuffer = new ByteArrayFilterGroup(null, "/frame0.jpg");

        // Shorts player components.
        StringFilterGroup pausedOverlayButtons = new StringFilterGroup(
                Settings.HIDE_SHORTS_PAUSED_OVERLAY_BUTTONS,
                "shorts_paused_state"
        );

        StringFilterGroup channelBar = new StringFilterGroup(
                Settings.HIDE_SHORTS_CHANNEL_BAR,
                REEL_CHANNEL_BAR_PATH
        );

        StringFilterGroup fullVideoLinkLabel = new StringFilterGroup(
                Settings.HIDE_SHORTS_FULL_VIDEO_LINK_LABEL,
                "reel_multi_format_link"
        );

        StringFilterGroup videoTitle = new StringFilterGroup(
                Settings.HIDE_SHORTS_VIDEO_TITLE,
                "shorts_video_title_item"
        );

        StringFilterGroup reelSoundMetadata = new StringFilterGroup(
                Settings.HIDE_SHORTS_SOUND_METADATA_LABEL,
                "reel_sound_metadata"
        );

        StringFilterGroup soundButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_SOUND_BUTTON,
                "reel_pivot_button"
        );

        StringFilterGroup infoPanel = new StringFilterGroup(
                Settings.HIDE_SHORTS_INFO_PANEL,
                "shorts_info_panel_overview"
        );

        StringFilterGroup stickers = new StringFilterGroup(
                Settings.HIDE_SHORTS_STICKERS,
                "stickers_layer.eml"
        );

        StringFilterGroup likeFountain = new StringFilterGroup(
                Settings.HIDE_SHORTS_LIKE_FOUNTAIN,
                "like_fountain.eml"
        );

        StringFilterGroup likeButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_LIKE_BUTTON,
                "shorts_like_button.eml",
                "reel_like_button.eml"
        );

        StringFilterGroup dislikeButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_DISLIKE_BUTTON,
                "shorts_dislike_button.eml",
                "reel_dislike_button.eml"
        );

        joinButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_JOIN_BUTTON,
                "sponsor_button"
        );

        subscribeButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_SUBSCRIBE_BUTTON,
                "subscribe_button"
        );

        paidPromotionButton = new StringFilterGroup(
                Settings.HIDE_PAID_PROMOTION_LABEL,
                "reel_player_disclosure.eml"
        );

        shortsActionBar = new StringFilterGroup(
                null,
                "shorts_action_bar.eml",
                "reel_action_bar.eml"
        );

        actionButton = new StringFilterGroup(
                null,
                // Can be simply 'button.eml', 'shorts_video_action_button.eml' or 'reel_action_button.eml'
                "button.eml"
        );

        suggestedAction = new StringFilterGroup(
                null,
                "suggested_action.eml"
        );

        addPathCallbacks(
                shortsCompactFeedVideoPath, joinButton, subscribeButton, paidPromotionButton,
                shortsActionBar, suggestedAction, pausedOverlayButtons, channelBar,
                fullVideoLinkLabel, videoTitle, reelSoundMetadata, soundButton, infoPanel,
                stickers, likeFountain, likeButton, dislikeButton
        );

        //
        // All other action buttons.
        //
        videoActionButtonGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_COMMENTS_BUTTON,
                        "reel_comment_button",
                        "youtube_shorts_comment_outline"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SHARE_BUTTON,
                        "reel_share_button",
                        "youtube_shorts_share_outline"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_REMIX_BUTTON,
                        "reel_remix_button",
                        "youtube_shorts_remix_outline"
                )
        );

        //
        // Suggested actions.
        //
        suggestedActionsGroupList.addAll(
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_PREVIEW_COMMENT,
                        // Preview comment that can popup while a Short is playing.
                        // Uses no bundled icons, and instead the users profile photo is shown.
                        "shorts-comments-panel"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SHOP_BUTTON,
                        "yt_outline_bag_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_TAGGED_PRODUCTS,
                        // Product buttons show pictures of the products, and does not have any unique icons to identify.
                        // Instead use a unique identifier found in the buffer.
                        "PAproduct_listZ"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_LOCATION_LABEL,
                        "yt_outline_location_point_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SAVE_SOUND_BUTTON,
                        "yt_outline_bookmark_",
                        // 'Save sound' button. It seems this has been removed and only 'Save music' is used.
                        // Still hide this in case it's still present.
                        "yt_outline_list_add_",
                        // 'Use this sound' button. It seems this has been removed and only 'Save music' is used.
                        // Still hide this in case it's still present.
                        "yt_outline_camera_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SEARCH_SUGGESTIONS,
                        "yt_outline_search_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_SUPER_THANKS_BUTTON,
                        "yt_outline_dollar_sign_heart_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_USE_TEMPLATE_BUTTON,
                        "yt_outline_template_add_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_UPCOMING_BUTTON,
                        "yt_outline_bell_"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_GREEN_SCREEN_BUTTON,
                        "greenscreen_temp"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_NEW_POSTS_BUTTON,
                        "yt_outline_box_pencil"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_SHORTS_HASHTAG_BUTTON,
                        "yt_outline_hashtag_"
                )
        );
    }

    private boolean isEverySuggestedActionFilterEnabled() {
        for (ByteArrayFilterGroup group : suggestedActionsGroupList) {
            if (!group.isEnabled()) {
                return false;
            }
        }

        return true;
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentType == FilterContentType.PATH) {
            if (matchedGroup == subscribeButton || matchedGroup == joinButton || matchedGroup == paidPromotionButton) {
                // Selectively filter to avoid false positive filtering of other subscribe/join buttons.
                return path.startsWith(REEL_CHANNEL_BAR_PATH) || path.startsWith(REEL_METAPANEL_PATH);
            }

            if (matchedGroup == shortsCompactFeedVideoPath) {
                return shouldHideShortsFeedItems() && shortsCompactFeedVideoBuffer.check(protobufBufferArray).isFiltered();
            }

            // Video action buttons (comment, share, remix) have the same path.
            // Like and dislike are separate path filters and don't require buffer searching.
            if (matchedGroup == shortsActionBar) {
                return actionButton.check(path).isFiltered()
                        && videoActionButtonGroupList.check(protobufBufferArray).isFiltered();
            }

            if (matchedGroup == suggestedAction) {
                // Skip searching the buffer if all suggested actions are set to hidden.
                // This has a secondary effect of hiding all new un-identified actions
                // under the assumption that the user wants all suggestions hidden.
                if (isEverySuggestedActionFilterEnabled()) {
                    return true;
                }

                return suggestedActionsGroupList.check(protobufBufferArray).isFiltered();
            }

            return true;
        }

        // Feed/search identifier components.
        if (matchedGroup == shelfHeader) {
            // Because the header is used in watch history and possibly other places, check for the index,
            // which is 0 when the shelf header is used for Shorts.
            if (contentIndex != 0) return false;
        }

        return shouldHideShortsFeedItems();
    }

    private static boolean shouldHideShortsFeedItems() {
        // Known issue if hide home is on but at least one other hide is off:
        //
        // Shorts suggestions will load in the background if a video is opened and
        // immediately minimized before any suggestions are loaded.
        // In this state the player type will show minimized, which cannot
        // distinguish between Shorts suggestions loading in the player and between
        // scrolling thru search/home/subscription tabs while a player is minimized.
        final boolean hideHome = Settings.HIDE_SHORTS_HOME.get();
        final boolean hideSubscriptions = Settings.HIDE_SHORTS_SUBSCRIPTIONS.get();
        final boolean hideSearch = Settings.HIDE_SHORTS_SEARCH.get();
        final boolean hideHistory = Settings.HIDE_SHORTS_HISTORY.get();

        if (!hideHome && !hideSubscriptions && !hideSearch && !hideHistory) {
            return false;
        }
        if (hideHome && hideSubscriptions && hideSearch && hideHistory) {
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
        if (!hideHome && !hideSubscriptions && !hideHistory) {
            return false;
        }

        // Check navigation absolutely last since the check may block this thread.
        NavigationButton selectedNavButton = NavigationButton.getSelectedNavigationButton();
        if (selectedNavButton == null) {
            return hideHome; // Unknown tab, treat the same as home.
        }

        return switch (selectedNavButton) {
            case HOME, EXPLORE -> hideHome;
            case SUBSCRIPTIONS -> hideSubscriptions;
            case LIBRARY -> hideHistory;
            default -> false;
        };
    }

    /**
     * Injection point.  Only used if patching older than 19.03.
     * This hook may be obsolete even for old versions
     * as they now use a litho layout like newer versions.
     */
    public static void hideShortsShelf(final View shortsShelfView) {
        if (shouldHideShortsFeedItems()) {
            Utils.hideViewByLayoutParams(shortsShelfView);
        }
    }

    public static int getSoundButtonSize(int original) {
        if (Settings.HIDE_SHORTS_SOUND_BUTTON.get()) {
            return 0;
        }

        return original;
    }

    public static void setNavigationBar(PivotBar view) {
        pivotBarRef = new WeakReference<>(view);
    }

    public static void hideNavigationBar(String tag) {
        if (HIDE_SHORTS_NAVIGATION_BAR) {
            if (REEL_WATCH_FRAGMENT_INIT_PLAYBACK.contains(tag)) {
                var pivotBar = pivotBarRef.get();
                if (pivotBar == null) return;

                Logger.printDebug(() -> "Hiding navbar by setting to GONE");
                pivotBar.setVisibility(View.GONE);
            } else {
                Logger.printDebug(() -> "Ignoring tag: " + tag);
            }
        }
    }

    public static int getNavigationBarHeight(int original) {
        if (HIDE_SHORTS_NAVIGATION_BAR) {
            return HIDDEN_NAVIGATION_BAR_VERTICAL_HEIGHT;
        }

        return original;
    }
}
