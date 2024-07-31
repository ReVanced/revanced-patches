package app.revanced.extension.twitch.patches;

import app.revanced.extension.twitch.settings.Settings;

@SuppressWarnings("unused")
public class VideoAdsPatch {
    public static boolean shouldBlockVideoAds() {
        return Settings.BLOCK_VIDEO_ADS.get();
    }
}