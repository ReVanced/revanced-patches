package app.revanced.integrations.patches;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import app.revanced.integrations.settings.SettingsEnum;

public final class DisableFineScrubbingGesturePatch {
    /**
     * Disables the fine scrubbing gesture.
     * @param tracker The velocity tracker that is used to determine the gesture.
     * @param event The motion event that is used to determine the gesture.
     */
    public static void disableGesture(VelocityTracker tracker, MotionEvent event) {
        if (SettingsEnum.DISABLE_FINE_SCRUBBING_GESTURE.getBoolean()) return;

        tracker.addMovement(event);
    }
}
