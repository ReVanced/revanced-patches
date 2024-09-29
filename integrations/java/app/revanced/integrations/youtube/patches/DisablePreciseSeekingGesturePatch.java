package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class DisablePreciseSeekingGesturePatch {
    public static boolean isGestureDisabled() {
        return Settings.DISABLE_PRECISE_SEEKING_GESTURE.get();
    }
}
