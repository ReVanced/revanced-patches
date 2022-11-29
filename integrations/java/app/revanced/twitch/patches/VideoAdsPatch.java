package app.revanced.twitch.patches;

import app.revanced.twitch.settings.SettingsEnum;

public class VideoAdsPatch {
    public static boolean shouldBlockVideoAds() {
        return SettingsEnum.BLOCK_VIDEO_ADS.getBoolean();
    }
}