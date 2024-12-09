package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideSeekbarPatch {
    public static boolean hideSeekbar() {
        return Settings.HIDE_SEEKBAR.get();
    }
}
