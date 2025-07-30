package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

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
                        } catch (Exception ex) {
                            Logger.printException(() -> "speed button reset failure", ex);
                        }
                        return true;
                    }
            );
        } catch (Exception ex) {
            Logger.printException(() -> "initializeButton failure", ex);
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
}