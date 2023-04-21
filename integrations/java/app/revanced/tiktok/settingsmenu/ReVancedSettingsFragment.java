package app.revanced.tiktok.settingsmenu;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.preference.EditTextPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;

import com.ss.android.ugc.aweme.splash.SplashActivity;

import app.revanced.tiktok.settings.SettingsEnum;
import app.revanced.tiktok.settings.SharedPrefCategory;
import app.revanced.tiktok.settingsmenu.preference.DownloadPathPreference;
import app.revanced.tiktok.utils.ReVancedUtils;

public class ReVancedSettingsFragment extends PreferenceFragment {

    private boolean Registered = false;
    private boolean settingsInitialized = false;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        for (SettingsEnum setting : SettingsEnum.values()) {
            if (!setting.path.equals(str)) continue;

            if (ReVancedUtils.getAppContext() != null && this.settingsInitialized && setting.rebootApp) {
                rebootDialog(getActivity());
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefCategory.TIKTOK_PREFS.prefName);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this.listener);
        this.Registered = true;

        final Activity context = this.getActivity();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        //Feed filter
        if (SettingsStatus.feedFilter) {
            PreferenceCategory feedFilter = new PreferenceCategory(context);
            feedFilter.setTitle("Feed filter");
            preferenceScreen.addPreference(feedFilter);

            //Remove ads toggle
            {
                SwitchPreference preference = new SwitchPreference(context);
                feedFilter.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_REMOVE_ADS.path);
                preference.setDefaultValue(SettingsEnum.TIK_REMOVE_ADS.defaultValue);
                preference.setChecked(SettingsEnum.TIK_REMOVE_ADS.getBoolean());
                preference.setTitle("Remove feed ads");
                preference.setSummary("Remove ads from feed.");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    // FIXME: the value is already saved in the preferences.
                    // instead of saving again, simple call SettingsEnum#setValue()
                    final boolean value = (Boolean) newValue;
                    SettingsEnum.TIK_REMOVE_ADS.saveValue(value);
                    return true;
                });
            }
            //Hide LiveStreams toggle
            {
                SwitchPreference preference = new SwitchPreference(context);
                feedFilter.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_HIDE_LIVE.path);
                preference.setDefaultValue(SettingsEnum.TIK_HIDE_LIVE.defaultValue);
                preference.setChecked(SettingsEnum.TIK_HIDE_LIVE.getBoolean());
                preference.setTitle("Hide livestreams");
                preference.setSummary("Hide livestreams from feed.");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final boolean value = (Boolean) newValue;
                    SettingsEnum.TIK_HIDE_LIVE.saveValue(value);
                    return true;
                });
            }
        }

        //Download
        if (SettingsStatus.download) {
            PreferenceCategory download = new PreferenceCategory(context);
            download.setTitle("Download");
            preferenceScreen.addPreference(download);
            //Download path
            {
                DownloadPathPreference preference = new DownloadPathPreference(context);
                download.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_DOWN_PATH.path);
                preference.setDefaultValue(SettingsEnum.TIK_DOWN_PATH.defaultValue);
                preference.setValue(SettingsEnum.TIK_DOWN_PATH.getString());
                preference.setTitle("Download path");
                preference.setSummary(Environment.getExternalStorageDirectory().getPath() + "/" + preference.getValue());
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final String value = (String) newValue;
                    SettingsEnum.TIK_DOWN_PATH.saveValue(value);
                    return true;
                });
            }
            //Download watermark
            {
                SwitchPreference preference = new SwitchPreference(context);
                download.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_DOWN_WATERMARK.path);
                preference.setDefaultValue(SettingsEnum.TIK_DOWN_WATERMARK.defaultValue);
                preference.setChecked(SettingsEnum.TIK_DOWN_WATERMARK.getBoolean());
                preference.setTitle("Remove watermark");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final boolean value = (Boolean) newValue;
                    SettingsEnum.TIK_DOWN_WATERMARK.saveValue(value);
                    return true;
                });
            }
        }

        // SpoofSimPatch
        if(SettingsStatus.simSpoof) {
            PreferenceCategory simSpoof = new PreferenceCategory(context);
            simSpoof.setTitle("Bypass regional restriction");
            preferenceScreen.addPreference(simSpoof);
            //Global Switch
            {
                SwitchPreference preference = new SwitchPreference(context);
                simSpoof.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_SIMSPOOF.path);
                preference.setDefaultValue(SettingsEnum.TIK_SIMSPOOF.defaultValue);
                preference.setChecked(SettingsEnum.TIK_SIMSPOOF.getBoolean());
                preference.setTitle("Fake sim card info");
                preference.setSummary("Bypass regional restriction by fake sim card information.");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final boolean value = (Boolean) newValue;
                    SettingsEnum.TIK_SIMSPOOF.saveValue(value);
                    return true;
                });
            }
            //Country ISO
            {
                EditTextPreference preference = new EditTextPreference(context);
                simSpoof.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_SIMSPOOF_ISO.path);
                preference.setDefaultValue(SettingsEnum.TIK_SIMSPOOF_ISO.defaultValue);
                preference.setText(SettingsEnum.TIK_SIMSPOOF_ISO.getString());
                preference.setTitle("Country ISO");
                preference.setSummary("us, uk, jp, ...");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final String value = (String) newValue;
                    SettingsEnum.TIK_SIMSPOOF_ISO.saveValue(value);
                    return true;
                });
            }
            //Operator mcc+mnc
            {
                EditTextPreference preference = new EditTextPreference(context);
                simSpoof.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_SIMSPOOF_MCCMNC.path);
                preference.setDefaultValue(SettingsEnum.TIK_SIMSPOOF_MCCMNC.defaultValue);
                preference.setText(SettingsEnum.TIK_SIMSPOOF_MCCMNC.getString());
                preference.setTitle("Operator mcc+mnc");
                preference.setSummary("mcc+mnc");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final String value = (String) newValue;
                    SettingsEnum.TIK_SIMSPOOF_MCCMNC.saveValue(value);
                    return true;
                });
            }
            //Operator name
            {
                EditTextPreference preference = new EditTextPreference(context);
                simSpoof.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_SIMSPOOF_OP_NAME.path);
                preference.setDefaultValue(SettingsEnum.TIK_SIMSPOOF_OP_NAME.defaultValue);
                preference.setText(SettingsEnum.TIK_SIMSPOOF_OP_NAME.getString());
                preference.setTitle("Operator name");
                preference.setSummary("Name of the operator");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final String value = (String) newValue;
                    SettingsEnum.TIK_SIMSPOOF_OP_NAME.saveValue(value);
                    return true;
                });
            }
        }

        //Integration
        PreferenceCategory integration = new PreferenceCategory(context);
        integration.setTitle("Integration");
        preferenceScreen.addPreference(integration);
        //Enable DebugLog toggle
        {
            SwitchPreference preference = new SwitchPreference(context);
            integration.addPreference(preference);
            preference.setKey(SettingsEnum.TIK_DEBUG.path);
            preference.setDefaultValue(SettingsEnum.TIK_DEBUG.defaultValue);
            preference.setChecked(SettingsEnum.TIK_DEBUG.getBoolean());
            preference.setTitle("Enable debug log");
            preference.setSummary("Show integration debug log.");
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                final boolean value = (Boolean) newValue;
                SettingsEnum.TIK_DEBUG.saveValue(value);
                return true;
            });
        }
        this.settingsInitialized = true;
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        if (this.Registered) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
            this.Registered = false;
        }
        super.onDestroy();
    }

    private void reboot(Activity activity) {
        int intent;
        intent = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        ((AlarmManager) activity.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.ELAPSED_REALTIME, 1500L, PendingIntent.getActivity(activity, 0, new Intent(activity, SplashActivity.class), intent));
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(final Activity activity) {
        new AlertDialog.Builder(activity).
                setMessage("Refresh and restart").
                setPositiveButton("RESTART", (dialog, i) -> reboot(activity))
                .setNegativeButton("CANCEL", null)
                .show();
    }
}
