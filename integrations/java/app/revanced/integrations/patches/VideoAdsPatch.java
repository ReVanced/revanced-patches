package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.whitelist.Whitelist;

public class VideoAdsPatch {

    //Used by app.revanced.patches.youtube.ad.general.video.patch.VideoAdsPatch
    //depends on Whitelist Patch. Still needs to be written
    public static boolean shouldShowAds() {
        return SettingsEnum.VIDEO_ADS_SHOWN.getBoolean() && Whitelist.shouldShowAds();
    }

}
