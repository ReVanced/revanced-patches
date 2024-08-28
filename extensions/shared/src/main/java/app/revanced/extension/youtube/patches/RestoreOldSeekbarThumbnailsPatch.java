package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class RestoreOldSeekbarThumbnailsPatch {
    public static boolean useFullscreenSeekbarThumbnails() {
        return !Settings.RESTORE_OLD_SEEKBAR_THUMBNAILS.get();
    }
}
