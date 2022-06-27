package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.videoswipecontrols.helpers.BrightnessHelper;

public class HDRMaxBrightnessPatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1rIJzsaMQouH_2__EcVY5Dg6l7ji9vsyP/view?usp=sharing for where it needs to be used.
    public static float getHDRBrightness(float original) {
        if (!SettingsEnum.USE_HDR_BRIGHTNESS_BOOLEAN.getBoolean()) return original;
        return SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean() ? BrightnessHelper.getBrightness() : -1.0f;
    }

}
