package app.revanced.extension.youtube.patches;

import androidx.annotation.NonNull;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.youtube.settings.Settings;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class SeekbarThumbnailsPatch {

    public static final class SeekbarThumbnailsHighQualityAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return VersionCheckPatch.IS_19_17_OR_GREATER || !Settings.RESTORE_OLD_SEEKBAR_THUMBNAILS.get();
        }

        @NonNull
        @Override
        public List<Setting<?>> getParentSettings() {
            return Collections.emptyList();
        }
    }

    private static final boolean SEEKBAR_THUMBNAILS_HIGH_QUALITY_ENABLED
            = Settings.SEEKBAR_THUMBNAILS_HIGH_QUALITY.get();

    /**
     * Injection point.
     */
    public static boolean useHighQualityFullscreenThumbnails() {
        return SEEKBAR_THUMBNAILS_HIGH_QUALITY_ENABLED;
    }

    /**
     * Injection point.
     */
    public static boolean useFullscreenSeekbarThumbnails() {
        return !Settings.RESTORE_OLD_SEEKBAR_THUMBNAILS.get();
    }
}
