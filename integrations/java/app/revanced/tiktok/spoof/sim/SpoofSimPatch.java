package app.revanced.tiktok.spoof.sim;

import app.revanced.tiktok.settings.SettingsEnum;

public class SpoofSimPatch {
    public static boolean isEnable() {
        return SettingsEnum.SIM_SPOOF.getBoolean();
    }
    public static String getCountryIso(String value) {
        if (isEnable()) {
            return SettingsEnum.SIM_SPOOF_ISO.getString();
        } else {
            return value;
        }

    }
    public static String getOperator(String value) {
        if (isEnable()) {
            return SettingsEnum.SIMSPOOF_MCCMNC.getString();
        } else {
            return value;
        }
    }
    public static String getOperatorName(String value) {
        if (isEnable()) {
            return SettingsEnum.SIMSPOOF_OP_NAME.getString();
        } else {
            return value;
        }
    }
}
