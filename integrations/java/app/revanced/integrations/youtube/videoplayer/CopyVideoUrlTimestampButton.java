package app.revanced.integrations.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.patches.CopyVideoUrlPatch;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;

public class CopyVideoUrlTimestampButton extends BottomControlButton {
    @Nullable
    private static CopyVideoUrlTimestampButton instance;

    public CopyVideoUrlTimestampButton(ViewGroup bottomControlsViewGroup) {
        super(
                bottomControlsViewGroup,
                "copy_video_url_timestamp_button",
                Settings.COPY_VIDEO_URL_TIMESTAMP,
                view -> CopyVideoUrlPatch.copyUrl(true),
                view -> {
                    CopyVideoUrlPatch.copyUrl(false);
                    return true;
                }
        );
    }

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
     * Injection point.
     */
    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }

}