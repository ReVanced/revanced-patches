package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableStartupAnimationPatch {

    /**
     * Injection point.
     */
    public static boolean showStartupAnimation(boolean original) {
        return original && !Settings.DISABLE_STARTUP_ANIMATION.get();
    }
}
