package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableSignInToTvPatch {
    public static boolean disableSignInToTv() {
        return Settings.DISABLE_SIGNIN_TO_TV.get();
    }
}
