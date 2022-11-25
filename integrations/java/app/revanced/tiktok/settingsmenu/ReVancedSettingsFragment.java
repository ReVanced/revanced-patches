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
import app.revanced.tiktok.settingsmenu.preference.DownloadPathPreference;
import app.revanced.tiktok.utils.ReVancedUtils;
import app.revanced.tiktok.utils.SharedPrefHelper;

public class ReVancedSettingsFragment extends PreferenceFragment {

    private boolean Registered = false;
    private boolean settingsInitialized = false;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        for (SettingsEnum setting : SettingsEnum.values()) {
            if (!setting.getPath().equals(str)) continue;

            if (ReVancedUtils.getAppContext() != null && this.settingsInitialized && setting.shouldRebootOnChange()) {
                rebootDialog(getActivity());
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefHelper.SharedPrefNames.TIKTOK_PREFS.getName());
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
                preference.setKey(SettingsEnum.TIK_REMOVE_ADS.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_REMOVE_ADS.getDefaultValue());
                preference.setChecked(SettingsEnum.TIK_REMOVE_ADS.getBoolean());
                preference.setTitle("Remove feed ads");
                preference.setSummary("Remove ads from feed.");
                preference.setOnPreferenceChangeListener((pref, newValue) -> {
                    final boolean value = (Boolean) newValue;
                    SettingsEnum.TIK_REMOVE_ADS.saveValue(value);
                    return true;
                });
            }
            //Hide LiveStreams toggle
            {
                SwitchPreference preference = new SwitchPreference(context);
                feedFilter.addPreference(preference);
                preference.setKey(SettingsEnum.TIK_HIDE_LIVE.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_HIDE_LIVE.getDefaultValue());
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
                preference.setKey(SettingsEnum.TIK_DOWN_PATH.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_DOWN_PATH.getDefaultValue());
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
                preference.setKey(SettingsEnum.TIK_DOWN_WATERMARK.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_DOWN_WATERMARK.getDefaultValue());
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
                preference.setKey(SettingsEnum.TIK_SIMSPOOF.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_SIMSPOOF.getDefaultValue());
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
                preference.setKey(SettingsEnum.TIK_SIMSPOOF_ISO.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_SIMSPOOF_ISO.getDefaultValue());
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
                preference.setKey(SettingsEnum.TIK_SIMSPOOF_MCCMNC.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_SIMSPOOF_MCCMNC.getDefaultValue());
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
                preference.setKey(SettingsEnum.TIK_SIMSPOOF_OP_NAME.getPath());
                preference.setDefaultValue(SettingsEnum.TIK_SIMSPOOF_OP_NAME.getDefaultValue());
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
            preference.setKey(SettingsEnum.TIK_DEBUG.getPath());
            preference.setDefaultValue(SettingsEnum.TIK_DEBUG.getDefaultValue());
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
