package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideGetPremiumPatch {
    /**
     * Injection point.
     */
    public static boolean hideGetPremiumView() {
        return Settings.HIDE_GET_PREMIUM.get();
    }
}
