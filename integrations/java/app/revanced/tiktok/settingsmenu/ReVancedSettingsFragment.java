package app.revanced.tiktok.settingsmenu;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;

import com.ss.android.ugc.aweme.splash.SplashActivity;

import app.revanced.tiktok.settings.SettingsEnum;
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
