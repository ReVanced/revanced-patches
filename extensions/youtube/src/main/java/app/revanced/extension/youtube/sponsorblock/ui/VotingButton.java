package app.revanced.extension.youtube.sponsorblock.ui;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockUtils;
import app.revanced.extension.youtube.videoplayer.PlayerControlButton;

public class VotingButton {
    @Nullable
    private static PlayerControlButton instance;

    public static void hideControls() {
        if (instance != null) instance.hide();
    }

    /**
     * injection point
     */
    public static void initialize(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_sb_voting_button",
                    null,
                    VotingButton::shouldBeShown,
                    v -> SponsorBlockUtils.onVotingClicked(v.getContext()),
                    null
            );
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * injection point
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) instance.setVisibilityImmediate(visible);
    }

    /**
     * injection point
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) instance.setVisibility(visible, animated);
    }

    private static boolean shouldBeShown() {
        return Settings.SB_ENABLED.get() && Settings.SB_VOTING_BUTTON.get()
                && SegmentPlaybackController.videoHasSegments() && !VideoInformation.isAtEndOfVideo();
    }
}
