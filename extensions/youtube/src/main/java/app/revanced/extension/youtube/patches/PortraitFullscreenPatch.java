package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class PortraitFullscreenPatch {

    /**
     * Injection point.
     */
    public static boolean openVideosInPortraitFullscreen() {
        return Settings.PORTRAIT_FULLSCREEN_MODE.get();
    }
}
