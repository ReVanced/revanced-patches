package app.revanced.twitch.settingsmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;

import app.revanced.twitch.settings.SettingsEnum;
import app.revanced.twitch.utils.LogHelper;
import app.revanced.twitch.utils.ReVancedUtils;
import tv.twitch.android.app.core.LandingActivity;

public class ReVancedSettingsFragment extends PreferenceFragment {

    private boolean registered = false;
    private boolean settingsInitialized = false;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        LogHelper.debug("Setting '%s' changed", key);
        syncPreference(key);
    };

    /**
     * Sync preference
     * @param key Preference to load. If key is null, all preferences are updated
     */
    private void syncPreference(@Nullable String key) {
        for (SettingsEnum setting : SettingsEnum.values()) {
            if (!setting.path.equals(key) && key != null)
                continue;

            Preference pref = this.findPreference(setting.path);
            LogHelper.debug("Syncing setting '%s' with UI", setting.path);

            if (pref instanceof SwitchPreference) {
                SettingsEnum.setValue(setting, ((SwitchPreference) pref).isChecked());
            }
            else if (pref instanceof EditTextPreference) {
                SettingsEnum.setValue(setting, ((EditTextPreference) pref).getText());
            }
            else if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                listPref.setSummary(listPref.getEntry());
                SettingsEnum.setValue(setting, listPref.getValue());
            }
            else {
                LogHelper.error("Setting '%s' cannot be handled!", pref);
            }

            if (ReVancedUtils.getContext() != null && key != null && settingsInitialized && setting.rebootApp) {
                rebootDialog(getActivity());
            }

            // First onChange event is caused by initial state loading
            this.settingsInitialized = true;
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            PreferenceManager mgr = getPreferenceManager();
            mgr.setSharedPreferencesName(SettingsEnum.REVANCED_PREFS);
            mgr.getSharedPreferences().registerOnSharedPreferenceChangeListener(this.listener);

            addPreferencesFromResource(
                getResources().getIdentifier(
                        SettingsEnum.REVANCED_PREFS,
                        "xml",
                        this.getContext().getPackageName()
                )
            );

            // Sync all preferences with UI
            syncPreference(null);

            this.registered = true;
        } catch (Throwable th) {
            LogHelper.printException("Error during onCreate()", th);
        }
    }

    @Override
    public void onDestroy() {
        if (this.registered) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
            this.registered = false;
        }
        super.onDestroy();
    }

    @SuppressLint("MissingPermission")
    private void reboot(Activity activity) {
        int flags;
        flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        ((AlarmManager) activity.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.ELAPSED_REALTIME, 1500L, PendingIntent.getActivity(activity, 0, new Intent(activity, LandingActivity.class), flags));
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(final Activity activity) {
        new AlertDialog.Builder(activity).
                setMessage(ReVancedUtils.getString("revanced_reboot_message")).
                setPositiveButton(ReVancedUtils.getString("revanced_reboot"), (dialog, i) -> reboot(activity))
                .setNegativeButton(ReVancedUtils.getString("revanced_cancel"), null)
                .show();
    }
}
