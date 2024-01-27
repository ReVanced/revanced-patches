package app.revanced.integrations.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.integrations.tiktok.settings.Settings;
import app.revanced.integrations.tiktok.settings.SettingsStatus;
import app.revanced.integrations.tiktok.settings.preference.InputTextPreference;
import app.revanced.integrations.tiktok.settings.preference.TogglePreference;

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
                Settings.SIM_SPOOF
        ));
        addPreference(new InputTextPreference(
                context,
                "Country ISO", "us, uk, jp, ...",
                Settings.SIM_SPOOF_ISO
        ));
        addPreference(new InputTextPreference(
                context,
                "Operator mcc+mnc", "mcc+mnc",
                Settings.SIMSPOOF_MCCMNC
        ));
        addPreference(new InputTextPreference(
                context,
                "Operator name", "Name of the operator.",
                Settings.SIMSPOOF_OP_NAME
        ));
    }
}
