package app.revanced.extension.youtube.sponsorblock.ui;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.videoplayer.PlayerControlTopButton;

public class CreateSegmentButtonController extends PlayerControlTopButton {
    @Nullable
    private static CreateSegmentButtonController instance;

    public static void hideControls() {
        if (instance != null) instance.hide();
    }

    /**
     * injection point
     */
    public static void initialize(View youtubeControlsLayout) {
        try {
            Logger.printDebug(() -> "initializing new segment button");
            ImageView imageView = Objects.requireNonNull(Utils.getChildViewByResourceName(
                    youtubeControlsLayout, "revanced_sb_create_segment_button"));
            instance = new CreateSegmentButtonController(imageView);
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

    private CreateSegmentButtonController(ImageView imageView) {
        super(imageView, v -> SponsorBlockViewController.toggleNewSegmentLayoutVisibility());
    }

    protected boolean shouldBeShown() {
        return Settings.SB_ENABLED.get() && Settings.SB_CREATE_NEW_SEGMENT.get()
                && !VideoInformation.isAtEndOfVideo();
    }
}
