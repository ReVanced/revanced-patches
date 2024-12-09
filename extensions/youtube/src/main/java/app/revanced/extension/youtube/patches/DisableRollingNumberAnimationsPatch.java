package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableRollingNumberAnimationsPatch {
    /**
     * Injection point.
     */
    public static boolean disableRollingNumberAnimations() {
        return Settings.DISABLE_ROLLING_NUMBER_ANIMATIONS.get();
    }
}
