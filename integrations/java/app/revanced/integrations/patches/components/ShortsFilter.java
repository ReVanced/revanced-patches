package app.revanced.integrations.patches.components;

import static app.revanced.integrations.utils.ReVancedUtils.hideViewBy1dpUnderCondition;
import static app.revanced.integrations.utils.ReVancedUtils.hideViewUnderCondition;

import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import app.revanced.integrations.settings.SettingsEnum;

public final class ShortsFilter extends Filter {
    private static final String REEL_CHANNEL_BAR_PATH = "reel_channel_bar.eml";
    public static PivotBar pivotBar; // Set by patch.

    private final StringFilterGroup channelBar;
    private final StringFilterGroup soundButton;
    private final StringFilterGroup infoPanel;
    private final StringFilterGroup shortsShelfHeader;

    public ShortsFilter() {
        // Home / subscription feed components.
        var thanksButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_THANKS_BUTTON,
                "suggested_action"
        );
        var shorts = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS,
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell",
                "shorts_pivot_item"
        );
        // Use a different filter group for this pattern, as it requires an additional check after matching.
        shortsShelfHeader = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS,
                "shelf_header.eml"
        );
        identifierFilterGroups.addAll(shorts, shortsShelfHeader, thanksButton);


        // Shorts player components.
        var joinButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_JOIN_BUTTON,
                "sponsor_button"
        );
        var subscribeButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SUBSCRIBE_BUTTON,
                "subscribe_button"
        );
        channelBar = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_CHANNEL_BAR,
                REEL_CHANNEL_BAR_PATH
        );
        soundButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SOUND_BUTTON,
                "reel_pivot_button"
        );
        infoPanel = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_INFO_PANEL,
                "shorts_info_panel_overview"
        );
        pathFilterGroupList.addAll(joinButton, subscribeButton, channelBar, soundButton, infoPanel);
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (matchedList == pathFilterGroupList) {
            // Always filter if matched.
            if (matchedGroup == soundButton || matchedGroup == infoPanel || matchedGroup == channelBar)
                return super.isFiltered(path, identifier, protobufBufferArray, matchedList, matchedGroup, matchedIndex);

            // Filter other path groups from pathFilterGroupList, only when reelChannelBar is visible
            // to avoid false positives.
            if (!path.startsWith(REEL_CHANNEL_BAR_PATH))
                return false;
        } else if (matchedGroup == shortsShelfHeader) {
            // Because the header is used in watch history and possibly other places, check for the index,
            // which is 0 when the shelf header is used for Shorts.
            if (matchedIndex != 0) return false;
        }

        // Super class handles logging.
        return super.isFiltered(path, identifier, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }

    public static void hideShortsShelf(final View shortsShelfView) {
        hideViewBy1dpUnderCondition(SettingsEnum.HIDE_SHORTS, shortsShelfView);
    }

    // Additional components that have to be hidden by setting their visibility

    public static void hideShortsCommentsButton(final View commentsButtonView) {
        hideViewUnderCondition(SettingsEnum.HIDE_SHORTS_COMMENTS_BUTTON, commentsButtonView);
    }

    public static void hideShortsRemixButton(final View remixButtonView) {
        hideViewUnderCondition(SettingsEnum.HIDE_SHORTS_REMIX_BUTTON, remixButtonView);
    }

    public static void hideShortsShareButton(final View shareButtonView) {
        hideViewUnderCondition(SettingsEnum.HIDE_SHORTS_SHARE_BUTTON, shareButtonView);
    }

    public static void hideNavigationBar() {
        if (!SettingsEnum.HIDE_SHORTS_NAVIGATION_BAR.getBoolean()) return;
        if (pivotBar == null) return;

        pivotBar.setVisibility(View.GONE);
    }

    public static View hideNavigationBar(final View navigationBarView) {
        if (SettingsEnum.HIDE_SHORTS_NAVIGATION_BAR.getBoolean())
            return null; // Hides the navigation bar.

        return navigationBarView;
    }
}
