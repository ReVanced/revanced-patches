package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HidePlayerButtonsPatch {

    /**
     * Injection point.
     */
    public static boolean previousOrNextButtonIsVisible(boolean previousOrNextButtonVisible) {
        if (Settings.HIDE_PLAYER_BUTTONS.get()) {
            return false;
        }
        return previousOrNextButtonVisible;
    }
}
