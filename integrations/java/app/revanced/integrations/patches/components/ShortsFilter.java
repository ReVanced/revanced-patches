package app.revanced.integrations.patches.components;

import static app.revanced.integrations.utils.ReVancedUtils.hideViewBy1dpUnderCondition;
import static app.revanced.integrations.utils.ReVancedUtils.hideViewUnderCondition;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;

import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import app.revanced.integrations.settings.SettingsEnum;

public final class ShortsFilter extends Filter {
    public static PivotBar pivotBar;
    @SuppressLint("StaticFieldLeak")

    private final StringFilterGroup reelChannelBar = new StringFilterGroup(
            null,
            "reel_channel_bar"
    );

    private final StringFilterGroup infoPanel = new StringFilterGroup(
            SettingsEnum.HIDE_SHORTS_INFO_PANEL,
            "shorts_info_panel_overview"
    );

    public ShortsFilter() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

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

        this.pathFilterGroups.addAll(joinButton, subscribeButton, soundButton, channelBar);
        this.identifierFilterGroups.addAll(shorts, thanksButton);
    }

    @Override
    boolean isFiltered(final String path, final String identifier,
                       final byte[] protobufBufferArray) {
        // Filter the path only when reelChannelBar is visible.
        if (reelChannelBar.check(path).isFiltered())
            if (this.pathFilterGroups.contains(path)) return true;

        // Shorts info panel path appears outside of reelChannelBar path.
        if (infoPanel.isEnabled() && infoPanel.check(path).isFiltered()) return true;

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
