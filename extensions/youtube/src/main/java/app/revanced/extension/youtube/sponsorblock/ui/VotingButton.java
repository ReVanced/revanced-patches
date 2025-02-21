package app.revanced.extension.youtube.sponsorblock.ui;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockUtils;
import app.revanced.extension.youtube.videoplayer.PlayerControlButton;

public class VotingButton extends PlayerControlButton {
    @Nullable
    private static VotingButton instance;

    public static void hideControls() {
        if (instance != null) instance.hide();
    }

    /**
     * injection point
     */
    public static void initialize(View controlsView) {
        try {
            Logger.printDebug(() -> "initializing voting button");
            instance = new VotingButton(controlsView);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Injection point
     */
    public static void changeVisibilityImmediate(boolean visible) {
        if (instance != null) instance.setVisibilityImmediate(visible);
    }

    /**
     * Injection point
     */
    public static void changeVisibility(boolean visible, boolean animated) {
        if (instance != null) instance.setVisibility(visible, animated);
    }

    /**
     * Injection point
     */
    public static void onPlayerTypeChanged(PlayerType newType) {
        if (instance != null) {
            Logger.printDebug(() -> "Player type changed to: " + newType);
            if (newType == PlayerType.WATCH_WHILE_MINIMIZED || newType.isMaximizedOrFullscreen()) {
                instance.syncVisibility();
            }
        }
    }

    private static boolean shouldBeShown() {
        return Settings.SB_ENABLED.get() && Settings.SB_VOTING_BUTTON.get()
                && SegmentPlaybackController.videoHasSegments() && !VideoInformation.isAtEndOfVideo();
    }

    private VotingButton(View imageView) {
        super(imageView,
                "revanced_sb_voting_button",
                null,
                VotingButton::shouldBeShown,
                v -> SponsorBlockUtils.onVotingClicked(imageView.getContext()),
                null);
    }
}
