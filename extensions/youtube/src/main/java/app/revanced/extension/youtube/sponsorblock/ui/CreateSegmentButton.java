package app.revanced.extension.youtube.sponsorblock.ui;

import android.view.View;

import android.view.ViewGroup;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.videoplayer.PlayerControlButton;

public class CreateSegmentButton extends PlayerControlButton {
    @Nullable
    private static CreateSegmentButton instance;

    public static void hideControls() {
        if (instance != null) instance.hide();
    }

    /**
     * injection point
     */
    public static void initialize(View controlsView) {
        try {
            Logger.printDebug(() -> "initializing new segment button");
            instance = new CreateSegmentButton((ViewGroup) controlsView);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * injection point
     */
    public static void changeVisibilityImmediate(boolean visible) {
        if (instance != null) instance.setVisibilityImmediate(visible);
    }

    /**
     * injection point
     */
    public static void changeVisibility(boolean visible, boolean animated) {
        if (instance != null) instance.setVisibility(visible, animated);
    }

    private static boolean shouldBeShown() {
        return Settings.SB_ENABLED.get() && Settings.SB_CREATE_NEW_SEGMENT.get()
                && !VideoInformation.isAtEndOfVideo();
    }

    private CreateSegmentButton(ViewGroup youtubeControlsLayout) {
        super(youtubeControlsLayout,
                "revanced_sb_create_segment_button",
                null,
                CreateSegmentButton::shouldBeShown,
                v -> SponsorBlockViewController.toggleNewSegmentLayoutVisibility(),
                null);
    }
}
