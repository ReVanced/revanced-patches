package app.revanced.extension.youtube.videoplayer;

import static app.revanced.extension.shared.StringRef.str;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.ResourceType;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class LoopVideoButton {
    @Nullable
    private static PlayerControlButton instance;

    private static final int LOOP_VIDEO_ON = Utils.getResourceIdentifierOrThrow(
            ResourceType.DRAWABLE, "revanced_loop_video_button_on");
    private static final int LOOP_VIDEO_OFF = Utils.getResourceIdentifierOrThrow(
            ResourceType.DRAWABLE, "revanced_loop_video_button_off");

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_loop_video_button",
                    null,
                    Settings.LOOP_VIDEO_BUTTON::get,
                    v -> updateButtonAppearance(true, v),
                    null
            );
            // Set icon when initializing button based on current setting
            updateButtonAppearance(false, null);
        } catch (Exception ex) {
            Logger.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * Animate button transition with fade and scale.
     */
    private static void animateButtonTransition(View view, boolean newState) {
        if (!(view instanceof ImageView imageView)) return;

        // Fade out.
        imageView.animate()
                .alpha(0.3f)
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(100)
                .withEndAction(() -> {
                    if (instance != null) {
                        instance.setIcon(newState ? LOOP_VIDEO_ON : LOOP_VIDEO_OFF);
                    }

                    // Fade in.
                    imageView.animate()
                            .alpha(1.0f)
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    /**
     * injection point.
     */
    public static void setVisibilityNegatedImmediate() {
        if (instance != null) instance.setVisibilityNegatedImmediate();
    }

    /**
     * injection point.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) instance.setVisibilityImmediate(visible);
        if (visible) {
            updateIconFromSettings();
        }
    }

    /**
     * injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) instance.setVisibility(visible, animated);
        if (visible) {
            updateIconFromSettings();
        }
    }

    /**
     * Update icon based on current setting value.
     */
    private static void updateIconFromSettings() {
        PlayerControlButton localInstance = instance;
        if (localInstance == null) return;

        final boolean currentState = Settings.LOOP_VIDEO.get();
        localInstance.setIcon(currentState ? LOOP_VIDEO_ON : LOOP_VIDEO_OFF);
    }

    /**
     * Updates the button's appearance.
     */
    private static void updateButtonAppearance(boolean userClickedButton, @Nullable View buttonView) {
        if (instance == null) return;

        try {
            Utils.verifyOnMainThread();

            final boolean currentState = Settings.LOOP_VIDEO.get();

            if (userClickedButton) {
                final boolean newState = !currentState;

                Settings.LOOP_VIDEO.save(newState);
                Utils.showToastShort(str(newState
                        ? "revanced_loop_video_button_toast_on"
                        : "revanced_loop_video_button_toast_off"));

                // Animate with the new state.
                if (buttonView != null) animateButtonTransition(buttonView, newState);
            } else {
                // Initialization - just set icon based on current state.
                instance.setIcon(currentState ? LOOP_VIDEO_ON : LOOP_VIDEO_OFF);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "updateButtonAppearance failure", ex);
        }
    }
}
