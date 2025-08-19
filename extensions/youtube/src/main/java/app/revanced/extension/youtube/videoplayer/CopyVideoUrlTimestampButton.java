package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.CopyVideoUrlPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class CopyVideoUrlTimestampButton {
    @Nullable
    private static PlayerControlButton instance;

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_copy_video_url_timestamp_button",
                    null,
                    Settings.COPY_VIDEO_URL_TIMESTAMP::get,
                    view -> CopyVideoUrlPatch.copyUrl(true),
                    view -> {
                        CopyVideoUrlPatch.copyUrl(false);
                        return true;
                    }
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
}