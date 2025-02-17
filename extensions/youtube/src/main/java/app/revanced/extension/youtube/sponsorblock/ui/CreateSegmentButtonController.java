package app.revanced.extension.youtube.sponsorblock.ui;

import static app.revanced.extension.youtube.videoplayer.PlayerControlButton.fadeInDuration;
import static app.revanced.extension.youtube.videoplayer.PlayerControlButton.fadeOutDuration;

import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

// Edit: This should be a subclass of PlayerControlButton
public class CreateSegmentButtonController {
    private static WeakReference<ImageView> buttonReference = new WeakReference<>(null);
    private static boolean isShowing;

    static final Animation fadeIn;
    static final Animation fadeOut;

    static {
        fadeIn = Utils.getResourceAnimation("fade_in");
        fadeIn.setDuration(fadeInDuration);

        fadeOut = Utils.getResourceAnimation("fade_out");
        fadeOut.setDuration(fadeOutDuration);
    }

    /**
     * injection point
     */
    public static void initialize(View youtubeControlsLayout) {
        try {
            Logger.printDebug(() -> "initializing new segment button");
            ImageView imageView = Objects.requireNonNull(Utils.getChildViewByResourceName(
                    youtubeControlsLayout, "revanced_sb_create_segment_button"));
            imageView.setVisibility(View.GONE);
            imageView.setOnClickListener(v -> SponsorBlockViewController.toggleNewSegmentLayoutVisibility());

            buttonReference = new WeakReference<>(imageView);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * injection point
     */
    public static void changeVisibilityImmediate(boolean visible) {
        setVisibility(visible, false);
    }

    /**
     * injection point
     */
    public static void changeVisibility(boolean visible, boolean animated) {
        // Ignore this call, otherwise with full screen thumbnails the buttons are visible while seeking.
        if (visible && !animated) return;

        setVisibility(visible, animated);
    }

    private static void setVisibility(boolean visible, boolean animated) {
        try {
            if (isShowing == visible) return;
            isShowing = visible;

            ImageView iView = buttonReference.get();
            if (iView == null) return;

            if (visible) {
                iView.clearAnimation();
                if (!shouldBeShown()) {
                    return;
                }
                if (animated) {
                    iView.startAnimation(fadeIn);
                }
                iView.setVisibility(View.VISIBLE);
                return;
            }

            if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(fadeOut);
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "changeVisibility failure", ex);
        }
    }

    private static boolean shouldBeShown() {
        return Settings.SB_ENABLED.get() && Settings.SB_CREATE_NEW_SEGMENT.get()
                && !VideoInformation.isAtEndOfVideo();
    }

    public static void hide() {
        if (!isShowing) {
            return;
        }
        Utils.verifyOnMainThread();
        View v = buttonReference.get();
        if (v == null) {
            return;
        }
        v.setVisibility(View.GONE);
        isShowing = false;
    }
}
