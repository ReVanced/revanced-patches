package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideGetPremiumPatch {
    /**
     * Injection point.
     */
    public static boolean hideGetPremiumView() {
        return SettingsEnum.HIDE_GET_PREMIUM.getBoolean();
    }
}
