package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableSignInToTVPopupPatch {

    /**
     * Injection point.
     */
    public static boolean disableSignInToTvPopup() {
        return Settings.DISABLE_SIGN_IN_TO_TV_POPUP.get();
    }
}
