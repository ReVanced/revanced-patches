package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class VideoAdsPatch {

    // Used by app.revanced.patches.youtube.ad.general.video.patch.VideoAdsPatch
    // depends on Whitelist patch (still needs to be written)
    public static boolean shouldShowAds() {
        return !SettingsEnum.HIDE_VIDEO_ADS.getBoolean(); // TODO && Whitelist.shouldShowAds();
    }

}
