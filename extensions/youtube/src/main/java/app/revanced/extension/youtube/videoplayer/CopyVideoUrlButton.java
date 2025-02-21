package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.CopyVideoUrlPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class CopyVideoUrlButton extends PlayerControlButton {
    @Nullable
    private static CopyVideoUrlButton instance;

    /**
     * Injection point.
     */
    public static void initializeButton(View view) {
        try {
            instance = new CopyVideoUrlButton((ViewGroup) view);
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

    public CopyVideoUrlButton(ViewGroup controlsView) {
        super(
                controlsView,
                "revanced_copy_video_url_button",
                "revanced_copy_video_url_button_placeholder",
                Settings.COPY_VIDEO_URL::get,
                view -> CopyVideoUrlPatch.copyUrl(false),
                view -> {
                    CopyVideoUrlPatch.copyUrl(true);
                    return true;
                }
        );
    }
}