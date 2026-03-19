package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class VideoAdsPatch {
    private static final boolean HIDE_VIDEO_ADS = Settings.HIDE_VIDEO_ADS.get();

    /**
     * Injection point.
     */
    public static boolean hideVideoAds() {
        return HIDE_VIDEO_ADS;
    }

    /**
     * Injection point.
     */
    public static String hideVideoAds(String osName) {
        return HIDE_VIDEO_ADS
                ? "Android Automotive"
                : osName;
    }

}
