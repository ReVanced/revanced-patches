package app.revanced.tiktok.spoof.sim;

import app.revanced.tiktok.settings.SettingsEnum;

public class SpoofSimPatch {
    public static boolean isEnable() {
        return SettingsEnum.TIK_SIMSPOOF.getBoolean();
    }
    public static String getCountryIso(String value) {
        if (isEnable()) {
            return SettingsEnum.TIK_SIMSPOOF_ISO.getString();
        } else {
            return value;
        }

    }
    public static String getOperator(String value) {
        if (isEnable()) {
            return SettingsEnum.TIK_SIMSPOOF_MCCMNC.getString();
        } else {
            return value;
        }
    }
    public static String getOperatorName(String value) {
        if (isEnable()) {
            return SettingsEnum.TIK_SIMSPOOF_OP_NAME.getString();
        } else {
            return value;
        }
    }
}
