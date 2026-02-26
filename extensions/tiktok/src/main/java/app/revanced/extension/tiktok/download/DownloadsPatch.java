package app.revanced.extension.tiktok.download;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.tiktok.settings.Settings;

@SuppressWarnings("unused")
public class DownloadsPatch {
    private static volatile String lastLoggedPath;
    private static volatile Boolean lastLoggedRemoveWatermark;

    public static String getDownloadPath() {
        String path = Settings.DOWNLOAD_PATH.get();
        if (BaseSettings.DEBUG.get() && (lastLoggedPath == null || !lastLoggedPath.equals(path))) {
            lastLoggedPath = path;
            Logger.printInfo(() -> "[ReVanced Downloads] download_path=\"" + path + "\"");
        }
        return path;
    }

    public static boolean shouldRemoveWatermark() {
        boolean removeWatermark = Settings.DOWNLOAD_WATERMARK.get();
        if (BaseSettings.DEBUG.get() && (lastLoggedRemoveWatermark == null || lastLoggedRemoveWatermark != removeWatermark)) {
            lastLoggedRemoveWatermark = removeWatermark;
            Logger.printInfo(() -> "[ReVanced Downloads] remove_watermark=" + removeWatermark);
        }
        return removeWatermark;
    }
}
