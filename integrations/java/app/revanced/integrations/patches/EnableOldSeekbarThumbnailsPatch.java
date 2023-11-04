package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class EnableOldSeekbarThumbnailsPatch {
    public static boolean enableOldSeekbarThumbnails() {
        return !SettingsEnum.ENABLE_OLD_SEEKBAR_THUMBNAILS.getBoolean();
    }
}
