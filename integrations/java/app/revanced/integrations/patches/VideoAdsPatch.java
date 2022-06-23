package app.revanced.integrations.patches;

import app.revanced.integrations.adremover.whitelist.Whitelist;

public class VideoAdsPatch {

    //Used by app.revanced.patches.youtube.ad.general.video.patch.VideoAdsPatch
    public static boolean shouldShowAds() {
        return Whitelist.shouldShowAds();
    }

}
