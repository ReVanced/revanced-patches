package app.revanced.integrations.youtube.patches.components;

import android.os.Build;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import app.revanced.integrations.youtube.settings.Settings;
import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import static app.revanced.integrations.shared.Utils.hideViewBy1dpUnderCondition;
import static app.revanced.integrations.shared.Utils.hideViewUnderCondition;

@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.N)
public final class ShortsFilter extends Filter {
    public static PivotBar pivotBar; // Set by patch.
    private final String REEL_CHANNEL_BAR_PATH = "reel_channel_bar.eml";

    private final StringFilterGroup channelBar;
    private final StringFilterGroup subscribeButton;
    private final StringFilterGroup subscribeButtonPaused;
    private final StringFilterGroup soundButton;
    private final StringFilterGroup infoPanel;
    private final StringFilterGroup shelfHeader;

    private final StringFilterGroup videoActionButton;
    private final ByteArrayFilterGroupList videoActionButtonGroupList = new ByteArrayFilterGroupList();

    public ShortsFilter() {
        var shorts = new StringFilterGroup(
                Settings.HIDE_SHORTS,
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell",
                "shorts_pivot_item"

        );
        // Feed Shorts shelf header.
        // Use a different filter group for this pattern, as it requires an additional check after matching.
        shelfHeader = new StringFilterGroup(
                Settings.HIDE_SHORTS,
                "shelf_header.eml"
        );

        // Home / subscription feed components.
        var thanksButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_THANKS_BUTTON,
                "suggested_action"
        );

        addIdentifierCallbacks(shorts, shelfHeader, thanksButton);

        // Shorts player components.
        var joinButton = new StringFilterGroup(
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

        soundButton = new StringFilterGroup(
                Settings.HIDE_SHORTS_SOUND_BUTTON,
                "reel_pivot_button"
        );

        infoPanel = new StringFilterGroup(
                Settings.HIDE_SHORTS_INFO_PANEL,
                "shorts_info_panel_overview"
        );

        videoActionButton = new StringFilterGroup(
                null,
                "ContainerType|shorts_video_action_button"
        );

        addPathCallbacks(
                joinButton, subscribeButton, subscribeButtonPaused,
                channelBar, soundButton, infoPanel, videoActionButton
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

        videoActionButtonGroupList.addAll(shortsCommentButton, shortsShareButton, shortsRemixButton);
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (contentType == FilterContentType.PATH) {
            // Always filter if matched.
            if (matchedGroup == soundButton ||
                    matchedGroup == infoPanel ||
                    matchedGroup == channelBar ||
                    matchedGroup == subscribeButtonPaused
            ) return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);

            // Video action buttons (comment, share, remix) have the same path.
            if (matchedGroup == videoActionButton) {
                if (videoActionButtonGroupList.check(protobufBufferArray).isFiltered()) return super.isFiltered(
                        identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex
                );
                return false;
            }

            // Filter other path groups from pathFilterGroupList, only when reelChannelBar is visible
            // to avoid false positives.
            if (path.startsWith(REEL_CHANNEL_BAR_PATH))
                if (matchedGroup == subscribeButton) return super.isFiltered(
                        identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex
                );

            return false;
        } else if (matchedGroup == shelfHeader) {
            // Because the header is used in watch history and possibly other places, check for the index,
            // which is 0 when the shelf header is used for Shorts.
            if (contentIndex != 0) return false;
        }

        // Super class handles logging.
        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }

    public static void hideShortsShelf(final View shortsShelfView) {
        hideViewBy1dpUnderCondition(Settings.HIDE_SHORTS, shortsShelfView);
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
