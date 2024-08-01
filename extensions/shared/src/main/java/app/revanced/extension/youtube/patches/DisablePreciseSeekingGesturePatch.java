package app.revanced.extension.youtube.patches;

import android.view.MotionEvent;
import android.view.VelocityTracker;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class DisablePreciseSeekingGesturePatch {
    /**
     * Disables the gesture that is used to seek precisely.
     * @param tracker The velocity tracker that is used to determine the gesture.
     * @param event The motion event that is used to determine the gesture.
     */
    public static void disableGesture(VelocityTracker tracker, MotionEvent event) {
        if (Settings.DISABLE_PRECISE_SEEKING_GESTURE.get()) return;

        tracker.addMovement(event);
    }
}
