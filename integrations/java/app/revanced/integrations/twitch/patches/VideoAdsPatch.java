package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.Settings;

@SuppressWarnings("unused")
public class VideoAdsPatch {
    public static boolean shouldBlockVideoAds() {
        return Settings.BLOCK_VIDEO_ADS.get();
    }
}