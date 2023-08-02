package app.revanced.tiktok.settingsmenu.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.tiktok.settings.SettingsEnum;
import app.revanced.tiktok.settingsmenu.SettingsStatus;
import app.revanced.tiktok.settingsmenu.preference.InputTextPreference;
import app.revanced.tiktok.settingsmenu.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class SimSpoofPreferenceCategory extends ConditionalPreferenceCategory {
    public SimSpoofPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Bypass regional restriction");
    }


    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.simSpoofEnabled;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new TogglePreference(
                context,
                "Fake sim card info",
                "Bypass regional restriction by fake sim card information.",
                SettingsEnum.SIM_SPOOF
        ));
        addPreference(new InputTextPreference(
                context,
                "Country ISO", "us, uk, jp, ...",
                SettingsEnum.SIM_SPOOF_ISO
        ));
        addPreference(new InputTextPreference(
                context,
                "Operator mcc+mnc", "mcc+mnc",
                SettingsEnum.SIMSPOOF_MCCMNC
        ));
        addPreference(new InputTextPreference(
                context,
                "Operator name", "Name of the operator.",
                SettingsEnum.SIMSPOOF_OP_NAME
        ));
    }
}
