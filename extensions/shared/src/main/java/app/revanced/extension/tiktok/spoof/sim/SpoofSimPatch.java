package app.revanced.extension.tiktok.spoof.sim;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.tiktok.settings.Settings;

@SuppressWarnings("unused")
public class SpoofSimPatch {

    private static final boolean ENABLED = Settings.SIM_SPOOF.get();

    public static String getCountryIso(String value) {
        if (ENABLED) {
            String iso = Settings.SIM_SPOOF_ISO.get();
            Logger.printDebug(() -> "Spoofing sim ISO from: " + value + " to: " + iso);
            return iso;
        }
        return value;
    }

    public static String getOperator(String value) {
        if (ENABLED) {
            String mcc_mnc = Settings.SIMSPOOF_MCCMNC.get();
            Logger.printDebug(() -> "Spoofing sim MCC-MNC from: " + value + " to: " + mcc_mnc);
            return mcc_mnc;
        }
        return value;
    }

    public static String getOperatorName(String value) {
        if (ENABLED) {
            String operator = Settings.SIMSPOOF_OP_NAME.get();
            Logger.printDebug(() -> "Spoofing sim operator from: " + value + " to: " + operator);
            return operator;
        }
        return value;
    }
}
