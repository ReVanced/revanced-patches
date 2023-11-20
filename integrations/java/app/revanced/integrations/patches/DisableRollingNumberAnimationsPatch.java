package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class DisableRollingNumberAnimationsPatch {
    /**
     * Injection point.
     */
    public static boolean disableRollingNumberAnimations() {
        return SettingsEnum.DISABLE_ROLLING_NUMBER_ANIMATIONS.getBoolean();
    }
}
