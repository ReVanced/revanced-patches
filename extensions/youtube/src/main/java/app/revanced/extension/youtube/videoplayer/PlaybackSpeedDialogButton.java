package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class PlaybackSpeedDialogButton extends PlayerControlButton {
    @Nullable
    private static PlaybackSpeedDialogButton instance;

    /**
     * Injection point.
     */
    public static void initializeButton(View view) {
        try {
            instance = new PlaybackSpeedDialogButton((ViewGroup) view);
        } catch (Exception ex) {
            Logger.printException(() -> "initializeButton failure", ex);
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

    public PlaybackSpeedDialogButton(ViewGroup controlsView) {
        super(
                controlsView,
                "revanced_playback_speed_dialog_button",
                "revanced_playback_speed_dialog_button_placeholder",
                Settings.PLAYBACK_SPEED_DIALOG_BUTTON::get,
                view -> CustomPlaybackSpeedPatch.showOldPlaybackSpeedMenu(),
                null
        );
    }
}