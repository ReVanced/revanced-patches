package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class RestoreOldSeekbarThumbnailsPatch {
    public static boolean useFullscreenSeekbarThumbnails() {
        return !Settings.RESTORE_OLD_SEEKBAR_THUMBNAILS.get();
    }
}
