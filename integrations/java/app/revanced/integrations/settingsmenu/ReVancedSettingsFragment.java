package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.StringRef.str;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.youtube.app.application.Shell_HomeActivity;

import app.revanced.integrations.patches.playback.speed.RememberPlaybackSpeedPatch;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.settings.SharedPrefCategory;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ReVancedSettingsFragment extends PreferenceFragment {
    /**
     * Used to prevent showing reboot dialog, if user cancels a setting user dialog.
     */
    private boolean showingUserDialogMessage;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            SettingsEnum setting = SettingsEnum.settingFromPath(str);
            if (setting == null) {
                return;
            }
            Preference pref = this.findPreference(str);
            LogHelper.printDebug(() -> "Setting " + setting.name() + " was changed. Preference " + str + ": " + pref);

            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                SettingsEnum.setValue(setting, switchPref.isChecked());
            } else if (pref instanceof EditTextPreference) {
                String editText = ((EditTextPreference) pref).getText();
                SettingsEnum.setValue(setting, editText);
            } else if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                SettingsEnum.setValue(setting, listPref.getValue());
                updateListPreferenceSummary((ListPreference) pref, setting);
            } else {
                LogHelper.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
                return;
            }

            if (!showingUserDialogMessage) {
                if (setting.userDialogMessage != null && ((SwitchPreference) pref).isChecked() != (Boolean) setting.defaultValue) {
                    showSettingUserDialogConfirmation(getActivity(), (SwitchPreference) pref, setting);
                } else if (setting.rebootApp) {
                    rebootDialog(getActivity());
                }
            }

            enableDisablePreferences();
        } catch (Exception ex) {
            LogHelper.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    @SuppressLint("ResourceType")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(SharedPrefCategory.YOUTUBE.prefName);
            addPreferencesFromResource(ReVancedUtils.getResourceIdentifier("revanced_prefs", "xml"));

            enableDisablePreferences();

            // if the preference was included, then initialize it based on the available playback speed
            Preference defaultSpeedPreference = findPreference(SettingsEnum.PLAYBACK_SPEED_DEFAULT.path);
            if (defaultSpeedPreference instanceof ListPreference) {
                RememberPlaybackSpeedPatch.initializeListPreference((ListPreference) defaultSpeedPreference);
            }

            // set the summary text for any ListPreferences
            for (SettingsEnum setting : SettingsEnum.values()) {
                Preference preference = findPreference(setting.path);
                if (preference instanceof ListPreference) {
                    updateListPreferenceSummary((ListPreference) preference, setting);
                }
            }

            preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        } catch (Exception ex) {
            LogHelper.printException(() -> "onActivityCreated() error", ex);
        }
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    private void enableDisablePreferences() {
        for (SettingsEnum setting : SettingsEnum.values()) {
            Preference preference = this.findPreference(setting.path);
            if (preference != null) {
                preference.setEnabled(setting.isAvailable());
            }
        }
    }

    private void updateListPreferenceSummary(ListPreference listPreference, SettingsEnum setting) {
        final int entryIndex = listPreference.findIndexOfValue(setting.getObjectValue().toString());
        if (entryIndex >= 0) {
            listPreference.setSummary(listPreference.getEntries()[entryIndex]);
        }
    }

    private void reboot(@NonNull Activity activity) {
        final int intentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent intent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, Shell_HomeActivity.class), intentFlags);
        AlarmManager systemService = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        systemService.setExact(AlarmManager.ELAPSED_REALTIME, 1500L, intent);
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(@NonNull Activity activity) {
        String positiveButton = str("in_app_update_restart_button");
        String negativeButton = str("sign_in_cancel");
        new AlertDialog.Builder(activity).setMessage(str("pref_refresh_config"))
                .setPositiveButton(positiveButton, (dialog, id) -> {
                    reboot(activity);
                })
                .setNegativeButton(negativeButton,  null)
                .setCancelable(false)
                .show();
    }

    private void showSettingUserDialogConfirmation(@NonNull Activity activity, SwitchPreference switchPref, SettingsEnum setting) {
        showingUserDialogMessage = true;
        new AlertDialog.Builder(activity)
                .setTitle(str("revanced_settings_confirm_user_dialog_title"))
                .setMessage(setting.userDialogMessage.toString())
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (setting.rebootApp) {
                        rebootDialog(activity);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    Boolean defaultBooleanValue = (Boolean) setting.defaultValue;
                    SettingsEnum.setValue(setting, defaultBooleanValue);
                    switchPref.setChecked(defaultBooleanValue);
                })
                .setOnDismissListener(dialog -> {
                    showingUserDialogMessage = false;
                })
                .setCancelable(false)
                .show();
    }
}
