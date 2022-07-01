package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.videoswipecontrols.helpers.BrightnessHelper;

public class HDRMaxBrightnessPatch {

    //Used by app/revanced/patches/youtube/misc/hdrbrightness/patch/HDRBrightnessPatch
    public static float getHDRBrightness(float original) {
        if (!SettingsEnum.USE_HDR_BRIGHTNESS_BOOLEAN.getBoolean()) return original;
        return SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean() ? BrightnessHelper.getBrightness() : -1.0f;
    }

}
