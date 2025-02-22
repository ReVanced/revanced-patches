package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
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
                    view -> CustomPlaybackSpeedPatch.showOldPlaybackSpeedMenu(),
                    null
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