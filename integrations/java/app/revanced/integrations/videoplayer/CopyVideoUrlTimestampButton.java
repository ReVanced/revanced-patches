package app.revanced.integrations.videoplayer;

import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.patches.CopyVideoUrlPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class CopyVideoUrlTimestampButton extends BottomControlButton {
    @Nullable
    private static CopyVideoUrlTimestampButton instance;

    public CopyVideoUrlTimestampButton(ViewGroup bottomControlsViewGroup) {
        super(
                bottomControlsViewGroup,
                "copy_video_url_timestamp_button",
                SettingsEnum.COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN,
                view -> CopyVideoUrlPatch.copyUrl(true)
        );
    }

    /**
     * Injection point.
     */
    public static void initializeButton(Object bottomControlsViewGroup) {
        try {
            instance = new CopyVideoUrlTimestampButton((ViewGroup) bottomControlsViewGroup);
        } catch (Exception ex) {
            LogHelper.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }

}