package app.revanced.extension.youtube.patches;

import android.os.VibrationEffect;
import android.os.Vibrator;

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
    public static boolean disablePreciseSeekingVibrate() {
        return Settings.DISABLE_HAPTIC_FEEDBACK_PRECISE_SEEKING.get();
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
    public static Object disableTapAndHoldVibrate(Object vibrator) {
        return Settings.DISABLE_HAPTIC_FEEDBACK_TAP_AND_HOLD.get()
                ? null
                : vibrator;
    }

    /**
     * Injection point.
     */
    public static boolean disableZoomVibrate() {
        return Settings.DISABLE_HAPTIC_FEEDBACK_ZOOM.get();
    }

    /**
     * Injection point.
     */
    public static void vibrate(Vibrator vibrator, VibrationEffect vibrationEffect) {
        if (disableVibrate()) return;
        vibrator.vibrate(vibrationEffect);
    }

    /**
     * Injection point.
     */
    @SuppressWarnings("deprecation")
    public static void vibrate(Vibrator vibrator, long milliseconds) {
        if (disableVibrate()) return;
        vibrator.vibrate(milliseconds);
    }

    private static boolean disableVibrate() {
        return Settings.DISABLE_HAPTIC_FEEDBACK_CHAPTERS.get()
                && Settings.DISABLE_HAPTIC_FEEDBACK_PRECISE_SEEKING.get()
                && Settings.DISABLE_HAPTIC_FEEDBACK_SEEK_UNDO.get()
                && Settings.DISABLE_HAPTIC_FEEDBACK_TAP_AND_HOLD.get()
                && Settings.DISABLE_HAPTIC_FEEDBACK_ZOOM.get();
    }
}
