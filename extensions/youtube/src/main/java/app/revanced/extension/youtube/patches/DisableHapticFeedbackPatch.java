package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableHapticFeedbackPatch {

    /**
     * Injection point.
     */
    public static boolean disableChapterVibrate() {
        return Settings.DISABLE_HAPTIC_FEEDBACK_CHAPTERS.get();
    }

    /**
     * Injection point.
     */
    public static boolean disableSeekUndoVibrate() {
        return Settings.DISABLE_HAPTIC_FEEDBACK_SEEK_UNDO.get();
    }

    /**
     * Injection point.
     */
    public static boolean disablePreciseSeekingVibrate() {
        return Settings.DISABLE_HAPTIC_FEEDBACK_PRECISE_SEEKING.get();
    }

    /**
     * Injection point.
     */
    public static boolean disableZoomVibrate() {
        return Settings.DISABLE_HAPTIC_FEEDBACK_ZOOM.get();
    }
}
