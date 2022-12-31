package app.revanced.integrations.videoplayer;

import app.revanced.integrations.patches.CopyVideoUrlPatch;
import app.revanced.integrations.settings.SettingsEnum;

public class CopyVideoUrlTimestampButton extends BottomControlButton {
    public static CopyVideoUrlTimestampButton instance;

    public CopyVideoUrlTimestampButton(Object obj) {
        super(
                obj,
                "copy_video_url_timestamp_button",
                SettingsEnum.COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN.getBoolean(),
                view -> CopyVideoUrlPatch.copyUrl(true)
        );
    }

    public static void initializeButton(Object obj) {
        instance = new CopyVideoUrlTimestampButton(obj);
    }

    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }

}