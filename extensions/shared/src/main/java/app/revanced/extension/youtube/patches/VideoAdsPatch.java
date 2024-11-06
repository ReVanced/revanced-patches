package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class VideoAdsPatch {

    // Used by app.revanced.patches.youtube.ad.general.video.patch.VideoAdsPatch
    // depends on Whitelist patch (still needs to be written)
    public static boolean shouldShowAds() {
        return !Settings.HIDE_VIDEO_ADS.get(); // TODO && Whitelist.shouldShowAds();
    }

}
