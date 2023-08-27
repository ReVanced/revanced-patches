package app.revanced.integrations.patches.components;

import static app.revanced.integrations.utils.ReVancedUtils.hideViewBy1dpUnderCondition;
import static app.revanced.integrations.utils.ReVancedUtils.hideViewUnderCondition;

import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import app.revanced.integrations.settings.SettingsEnum;

public final class ShortsFilter extends Filter {
    private static final String REEL_CHANNEL_BAR_PATH = "reel_channel_bar";
    public static PivotBar pivotBar; // Set by patch.

    private final StringFilterGroup channelBar;
    private final StringFilterGroup soundButton;
    private final StringFilterGroup infoPanel;

    public ShortsFilter() {
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

        final var thanksButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_THANKS_BUTTON,
                "suggested_action"
        );

        final var subscribeButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SUBSCRIBE_BUTTON,
                "subscribe_button"
        );

        final var joinButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_JOIN_BUTTON,
                "sponsor_button"
        );

        final var shorts = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS,
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell",
                "shorts_pivot_item"
        );

        pathFilterGroups.addAll(joinButton, subscribeButton, channelBar, soundButton, infoPanel);
        identifierFilterGroups.addAll(shorts, thanksButton);
    }

    @Override
    boolean isFiltered(String path, @Nullable String identifier, byte[] protobufBufferArray,
                       FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (matchedGroup == soundButton || matchedGroup == infoPanel || matchedGroup == channelBar) return true;

        // Filter the path only when reelChannelBar is visible.
        if (pathFilterGroups == matchedList) {
            return path.contains(REEL_CHANNEL_BAR_PATH);
        }

        return identifierFilterGroups == matchedList;
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
