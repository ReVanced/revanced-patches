package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideAutoplayButtonPatch {

    private static final Boolean HIDE_AUTOPLAY_BUTTON_ENABLED = Settings.HIDE_AUTOPLAY_BUTTON.get();

    /**
     * Injection point.
     */
    public static boolean hideAutoPlayButton() {
        return HIDE_AUTOPLAY_BUTTON_ENABLED;
    }
}
