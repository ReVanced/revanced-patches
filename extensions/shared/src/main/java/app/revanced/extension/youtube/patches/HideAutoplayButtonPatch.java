package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideAutoplayButtonPatch {

    private static final boolean HIDE_AUTOPLAY_BUTTON_ENABLED = Settings.HIDE_AUTOPLAY_BUTTON.get();

    /**
     * Injection point.
     */
    public static boolean hideAutoPlayButton() {
        return HIDE_AUTOPLAY_BUTTON_ENABLED;
    }
}
