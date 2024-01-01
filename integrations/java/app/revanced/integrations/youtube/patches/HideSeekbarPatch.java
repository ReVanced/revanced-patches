package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideSeekbarPatch {
    public static boolean hideSeekbar() {
        return Settings.HIDE_SEEKBAR.get();
    }
}
