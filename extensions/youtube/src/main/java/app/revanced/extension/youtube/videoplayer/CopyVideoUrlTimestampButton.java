package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.CopyVideoUrlPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class CopyVideoUrlTimestampButton extends PlayerControlButton {
    @Nullable
    private static CopyVideoUrlTimestampButton instance;

    /**
     * Injection point.
     */
    public static void initializeButton(View bottomControlsViewGroup) {
        try {
            instance = new CopyVideoUrlTimestampButton((ViewGroup) bottomControlsViewGroup);
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


    public CopyVideoUrlTimestampButton(ViewGroup controlsView) {
        super(
                controlsView,
                "revanced_copy_video_url_timestamp_button",
                "revanced_copy_video_url_timestamp_button_placeholder",
                Settings.COPY_VIDEO_URL_TIMESTAMP::get,
                view -> CopyVideoUrlPatch.copyUrl(true),
                view -> {
                    CopyVideoUrlPatch.copyUrl(false);
                    return true;
                }
        );
    }
}