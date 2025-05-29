package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.showToastShort;

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
                    view -> { CustomPlaybackSpeedPatch.showModernCustomPlaybackSpeedDialog(view.getContext());},
                    view -> {
                        if (!Settings.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED.get() ||
                                VideoInformation.getPlaybackSpeed() == Settings.PLAYBACK_SPEED_DEFAULT.get()) {
                            VideoInformation.overridePlaybackSpeed(1.0f);
                            showToastShort(str("revanced_custom_playback_speeds_reset_toast", "1.0"));
                        } else {
                            float defaultSpeed = Settings.PLAYBACK_SPEED_DEFAULT.get();
                            VideoInformation.overridePlaybackSpeed(defaultSpeed);
                            showToastShort(str("revanced_custom_playback_speeds_reset_toast", defaultSpeed));
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