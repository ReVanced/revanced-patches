package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class PlaybackSpeedDialogButton {

    @Nullable
    private static PlayerControlButton instance;

    private static final DecimalFormat speedDecimalFormatter = new DecimalFormat();
    static {
        speedDecimalFormatter.setMinimumFractionDigits(1);
        speedDecimalFormatter.setMaximumFractionDigits(2);
    }

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_playback_speed_dialog_button_container",
                    "revanced_playback_speed_dialog_button",
                    "revanced_playback_speed_dialog_button_text",
                    Settings.PLAYBACK_SPEED_DIALOG_BUTTON::get,
                    view -> {
                        try {
                            if (Settings.RESTORE_OLD_SPEED_MENU.get()) {
                                CustomPlaybackSpeedPatch.showOldPlaybackSpeedMenu();
                            } else {
                                CustomPlaybackSpeedPatch.showModernCustomPlaybackSpeedDialog(view.getContext());
                            }
                        } catch (Exception ex) {
                            Logger.printException(() -> "speed button onClick failure", ex);
                        }
                    },
                    view -> {
                        try {
                            final float defaultSpeed = Settings.PLAYBACK_SPEED_DEFAULT.get();
                            final float speed = (!Settings.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED.get() ||
                                    VideoInformation.getPlaybackSpeed() == defaultSpeed)
                                    ? 1.0f
                                    : defaultSpeed;
                            VideoInformation.overridePlaybackSpeed(speed);
                        } catch (Exception ex) {
                            Logger.printException(() -> "speed button reset failure", ex);
                        }
                        return true;
                    }
            );

            // Set the appropriate icon.
            updateButtonAppearance();
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
     * Injection point.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) {
            instance.setVisibilityImmediate(visible);
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) {
            instance.setVisibility(visible, animated);
        }
    }

    /**
     * Injection point.
     */
    public static void videoSpeedChanged(float currentVideoSpeed) {
        updateButtonAppearance();
    }

    /**
     * Updates the button's appearance, including icon and text overlay.
     */
    private static void updateButtonAppearance() {
        if (instance == null) return;

        try {
            Utils.verifyOnMainThread();

            String speedText = speedDecimalFormatter.format(VideoInformation.getPlaybackSpeed());
            instance.setTextOverlay(speedText);
            Logger.printDebug(() -> "Updated playback speed button text to: " + speedText);
        } catch (Exception ex) {
            Logger.printException(() -> "updateButtonAppearance failure", ex);
        }
    }
}
