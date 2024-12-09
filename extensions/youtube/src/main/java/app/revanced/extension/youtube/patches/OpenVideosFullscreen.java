package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class OpenVideosFullscreen {

    /**
     * Injection point.
     */
    public static boolean openVideoFullscreenPortrait(boolean original) {
        return Settings.OPEN_VIDEOS_FULLSCREEN_PORTRAIT.get();
    }
}
