package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class VideoAdsPatch {

    // Used by app.revanced.patches.youtube.ad.general.video.patch.VideoAdsPatch
    // depends on Whitelist patch (still needs to be written)
    public static boolean shouldShowAds() {
        return !Settings.HIDE_VIDEO_ADS.get(); // TODO && Whitelist.shouldShowAds();
    }

}
