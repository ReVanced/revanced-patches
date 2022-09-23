package app.revanced.tiktok.download;

import app.revanced.tiktok.settings.SettingsEnum;

public class DownloadsPatch {
    public static String getDownloadPath() {
        return SettingsEnum.TIK_DOWN_PATH.getString();
    }

    public static boolean shouldRemoveWatermark() {
        return SettingsEnum.TIK_DOWN_WATERMARK.getBoolean();
    }
}
