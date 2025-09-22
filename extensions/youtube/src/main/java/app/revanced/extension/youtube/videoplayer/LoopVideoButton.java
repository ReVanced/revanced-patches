package app.revanced.extension.youtube.videoplayer;

import static app.revanced.extension.shared.StringRef.str;

import android.view.View;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class LoopVideoButton {
    @Nullable
    private static PlayerControlButton instance;

    private static final int LOOP_VIDEO_ON = Utils.getResourceIdentifierOrThrow(
            "revanced_loop_video_button_on", "drawable");
    private static final int LOOP_VIDEO_OFF = Utils.getResourceIdentifierOrThrow(
            "revanced_loop_video_button_off", "drawable");

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
                    v -> updateButtonAppearance(),
                    null
            );
        } catch (Exception ex) {
            Logger.printException(() -> "initializeButton failure", ex);
        }
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
    }

    /**
     * injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) instance.setVisibility(visible, animated);
    }

    /**
     * Updates the button's appearance.
     */
    private static void updateButtonAppearance() {
        if (instance == null) return;

        try {
            Utils.verifyOnMainThread();

            final boolean currentState = Settings.LOOP_VIDEO.get();
            final boolean newState = !currentState;
            Settings.LOOP_VIDEO.save(newState);

            instance.setIcon(newState
                    ? LOOP_VIDEO_ON
                    : LOOP_VIDEO_OFF);
            Utils.showToastShort(str(newState
                    ? "revanced_loop_video_button_toast_on"
                    : "revanced_loop_video_button_toast_off"));
        } catch (Exception ex) {
            Logger.printException(() -> "updateButtonAppearance failure", ex);
        }
    }
}
