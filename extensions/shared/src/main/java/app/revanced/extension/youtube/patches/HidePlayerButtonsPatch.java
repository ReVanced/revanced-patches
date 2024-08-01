package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

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
