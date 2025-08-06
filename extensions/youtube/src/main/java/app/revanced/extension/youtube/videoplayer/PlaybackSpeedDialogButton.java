package app.revanced.extension.youtube.videoplayer;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

import java.text.DecimalFormat;

@SuppressWarnings("unused")
public class PlaybackSpeedDialogButton {
    @Nullable
    private static PlayerControlButton instance;

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_playback_speed_dialog_button",
                    "revanced_playback_speed_dialog_button_placeholder",
                    isCustomSpeedMenuEnabled() ? "revanced_playback_speed_dialog_button_text" : null,
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
                            final float speed = (!Settings.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED.get() ||
                                    VideoInformation.getPlaybackSpeed() == Settings.PLAYBACK_SPEED_DEFAULT.get())
                                    ? 1.0f
                                    : Settings.PLAYBACK_SPEED_DEFAULT.get();

                            VideoInformation.overridePlaybackSpeed(speed);
                            updateButtonAppearance();
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
     * Injection point.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) {
            instance.setVisibilityImmediate(visible);
            if (visible) updateButtonAppearance();
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) {
            instance.setVisibility(visible, animated);
            if (visible) updateButtonAppearance();
        }
    }

    /**
     * Determines if the modern custom speed menu is enabled.
     */
    private static boolean isCustomSpeedMenuEnabled() {
        return Settings.CUSTOM_SPEED_MENU.get() && !Settings.RESTORE_OLD_SPEED_MENU.get();
    }

    /**
     * Updates the button's appearance, including icon and text overlay.
     */
    public static void updateButtonAppearance() {
        if (instance == null) return;

        try {
            String drawableName = isCustomSpeedMenuEnabled()
                    ? "revanced_playback_speed_dialog_button_rectangle"
                    : "revanced_playback_speed_dialog_button";
            int drawableId = Utils.getResourceIdentifier(drawableName, "drawable");
            instance.setIcon(drawableId);

            if (isCustomSpeedMenuEnabled()) {
                float currentSpeed = VideoInformation.getPlaybackSpeed();
                String speedText = formatSpeedText(currentSpeed);
                instance.setTextOverlay(speedText);
                Logger.printDebug(() -> "Updated playback speed button text to: " + speedText);
            } else {
                instance.setTextOverlay(null); // Clear text overlay.
            }
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to update button appearance", ex);
        }
    }


    @NonNull
    @SuppressLint("DefaultLocale")
    private static String formatSpeedText(float speed) {
        DecimalFormat df = new DecimalFormat(speed % 1 == 0 ? "#.0" : "#.##");
        return df.format(speed);
    }
}
