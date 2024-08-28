package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.patches.CopyVideoUrlPatch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.shared.Logger;

public class CopyVideoUrlButton extends BottomControlButton {
    @Nullable
    private static CopyVideoUrlButton instance;

    public CopyVideoUrlButton(ViewGroup viewGroup) {
        super(
                viewGroup,
                "revanced_copy_video_url_button",
                Settings.COPY_VIDEO_URL,
                view -> CopyVideoUrlPatch.copyUrl(false),
                view -> {
                    CopyVideoUrlPatch.copyUrl(true);
                    return true;
                }
        );
    }

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
     * Injection point.
     */
    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }
}