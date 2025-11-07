package app.revanced.extension.music.patches;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideHistoryButtonPatch {

    /**
     * Injection point
     */
    public static boolean hideHistoryButton(boolean original) {
        return !Settings.HIDE_HISTORY_BUTTON.get() && original;
    }
}
