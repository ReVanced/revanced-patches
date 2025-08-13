package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableSignInToTvPopupPatch {

    /**
     * Injection point.
     */
    public static boolean disableSignInToTvPopup() {
        return Settings.DISABLE_SIGNIN_TO_TV_POPUP.get();
    }
}
