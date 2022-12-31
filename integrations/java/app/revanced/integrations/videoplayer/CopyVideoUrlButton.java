package app.revanced.integrations.videoplayer;


import app.revanced.integrations.patches.CopyVideoUrlPatch;
import app.revanced.integrations.settings.SettingsEnum;

public class CopyVideoUrlButton extends BottomControlButton {
    public static CopyVideoUrlButton instance;

    public CopyVideoUrlButton(Object obj) {
        super(
                obj,
                "copy_video_url_button",
                SettingsEnum.COPY_VIDEO_URL_BUTTON_SHOWN.getBoolean(),
                view -> CopyVideoUrlPatch.copyUrl(false)
        );
    }

    public static void initializeButton(Object obj) {
        instance = new CopyVideoUrlButton(obj);
    }

    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }
}