package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class VideoAdsPatch {

    private static final boolean SHOW_VIDEO_ADS = !Settings.HIDE_VIDEO_ADS.get();

    /**
     * Injection point.
     */
    public static boolean shouldShowAds() {
        return SHOW_VIDEO_ADS;
    }

}
