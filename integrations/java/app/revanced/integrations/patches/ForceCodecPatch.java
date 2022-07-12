package app.revanced.integrations.patches;

import android.os.Build;

import app.revanced.integrations.settings.SettingsEnum;

public class ForceCodecPatch {

    public static String getManufacturer() {
        return SettingsEnum.CODEC_OVERRIDE_BOOLEAN.getBoolean() ? "samsung" : Build.MANUFACTURER;
    }

    public static String getModel() {
        return SettingsEnum.CODEC_OVERRIDE_BOOLEAN.getBoolean() ? "SM-G920F" : Build.MODEL;
    }

    public static boolean shouldForceVP9() {
        return SettingsEnum.CODEC_OVERRIDE_BOOLEAN.getBoolean();
    }


}
