package app.revanced.integrations.patches.components;

import android.view.View;
import app.revanced.integrations.settings.SettingsEnum;
import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import static app.revanced.integrations.utils.ReVancedUtils.hideViewBy1dpUnderCondition;
import static app.revanced.integrations.utils.ReVancedUtils.hideViewUnderCondition;

public final class ShortsFilter extends Filter {
    // Set by patch.
    public static PivotBar pivotBar;
    final StringFilterGroupList shortsFilterGroup = new StringFilterGroupList();
    private final StringFilterGroup reelChannelBar = new StringFilterGroup(
            null,
            "reel_channel_bar"
    );

    public ShortsFilter() {
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

        final var soundButton = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_SOUND_BUTTON,
                "reel_pivot_button"
        );

        final var infoPanel = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_INFO_PANEL,
                "shorts_info_panel_overview"
        );

        final var channelBar = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS_CHANNEL_BAR,
                "reel_channel_bar"
        );

        final var shorts = new StringFilterGroup(
                SettingsEnum.HIDE_SHORTS,
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell"
        );

        shortsFilterGroup.addAll(soundButton, infoPanel);
        pathFilterGroups.addAll(joinButton, subscribeButton, channelBar);
        identifierFilterGroups.addAll(shorts, thanksButton);
    }

    @Override
    boolean isFiltered(final String path, final String identifier,
                       final byte[] protobufBufferArray) {

        // Filter the path only when reelChannelBar is visible.
        if (reelChannelBar.check(path).isFiltered())
            if (this.pathFilterGroups.contains(path)) return true;

        if (shortsFilterGroup.contains(path)) return true;

        return this.identifierFilterGroups.contains(identifier);
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
