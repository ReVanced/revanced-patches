package app.revanced.integrations.tiktok.spoof.sim;

import app.revanced.integrations.tiktok.settings.Settings;

@SuppressWarnings("unused")
public class SpoofSimPatch {
    public static boolean isEnable() {
        return Settings.SIM_SPOOF.get();
    }
    public static String getCountryIso(String value) {
        if (isEnable()) {
            return Settings.SIM_SPOOF_ISO.get();
        } else {
            return value;
        }

    }
    public static String getOperator(String value) {
        if (isEnable()) {
            return Settings.SIMSPOOF_MCCMNC.get();
        } else {
            return value;
        }
    }
    public static String getOperatorName(String value) {
        if (isEnable()) {
            return Settings.SIMSPOOF_OP_NAME.get();
        } else {
            return value;
        }
    }
}
