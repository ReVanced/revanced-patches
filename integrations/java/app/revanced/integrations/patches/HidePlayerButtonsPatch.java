package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class HidePlayerButtonsPatch {

    /**
     * Injection point.
     */
    public static boolean previousOrNextButtonIsVisible(boolean previousOrNextButtonVisible) {
        if (SettingsEnum.HIDE_PLAYER_BUTTONS.getBoolean()) {
            return false;
        }
        return previousOrNextButtonVisible;
    }
}
