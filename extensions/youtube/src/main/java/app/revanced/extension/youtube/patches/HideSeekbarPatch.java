package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideSeekbarPatch {
    /**
     * Injection point.
     */
    public static boolean hideSeekbar() {
        return Settings.HIDE_SEEKBAR.get();
    }

    /**
     * Injection point.
     */
    public static boolean useFullscreenLargeSeekbar(boolean original) {
        return Settings.FULLSCREEN_LARGE_SEEKBAR.get();
    }
}
