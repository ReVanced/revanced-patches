package app.revanced.extension.music.patches;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideVideoAdsPatch {

    /**
     * Injection point
     */
    public static boolean hideVideoAds() {
        return Settings.HIDE_VIDEO_ADS.get();
    }
}
