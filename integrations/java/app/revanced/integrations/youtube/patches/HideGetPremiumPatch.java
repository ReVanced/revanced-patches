package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideGetPremiumPatch {
    /**
     * Injection point.
     */
    public static boolean hideGetPremiumView() {
        return Settings.HIDE_GET_PREMIUM.get();
    }
}
