package app.revanced.extension.music.patches;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideUpgradeButtonPatch {

    /**
     * Injection point
     */
    public static boolean hideUpgradeButton() {
        return Settings.HIDE_UPGRADE_BUTTON.get();
    }
}
