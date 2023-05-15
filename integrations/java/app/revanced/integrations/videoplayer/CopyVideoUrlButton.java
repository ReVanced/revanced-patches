package app.revanced.integrations.videoplayer;

import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.patches.CopyVideoUrlPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class CopyVideoUrlButton extends BottomControlButton {
    @Nullable
    private static CopyVideoUrlButton instance;

    public CopyVideoUrlButton(ViewGroup viewGroup) {
        super(
                viewGroup,
                "copy_video_url_button",
                SettingsEnum.COPY_VIDEO_URL,
                view -> CopyVideoUrlPatch.copyUrl(false)
        );
    }

    /**
     * Injection point.
     */
    public static void initializeButton(Object obj) {
        try {
            instance = new CopyVideoUrlButton((ViewGroup) obj);
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