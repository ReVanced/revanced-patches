package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

@SuppressWarnings("unused")
public final class RestoreOldSeekbarThumbnailsPatch {
    public static boolean useFullscreenSeekbarThumbnails() {
        return !SettingsEnum.RESTORE_OLD_SEEKBAR_THUMBNAILS.getBoolean();
    }
}
