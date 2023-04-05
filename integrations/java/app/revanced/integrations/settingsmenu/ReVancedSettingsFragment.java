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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.apps.youtube.app.application.Shell_HomeActivity;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.settings.SharedPrefCategory;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ReVancedSettingsFragment extends PreferenceFragment {
    /**
     * If a dialog is currently being shown.  Used to prevent showing additional dialogs if user cancels a dialog.
     */
    private boolean currentlyShowingDialog;

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
                Object value;
                switch (setting.returnType) {
                    case INTEGER:
                        value = Integer.parseInt(editText);
                        break;
                    case LONG:
                        value = Long.parseLong(editText);
                        break;
                    case FLOAT:
                        value = Float.parseFloat(editText);
                        break;
                    case STRING:
                        value = editText;
                        break;
                    default:
                        throw new IllegalStateException(setting.toString());
                }
                SettingsEnum.setValue(setting, value);
            } else {
                LogHelper.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
            }

            if (!currentlyShowingDialog) {
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

    private void reboot(@NonNull Activity activity) {
        final int intentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent intent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, Shell_HomeActivity.class), intentFlags);
        AlarmManager systemService = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        systemService.setExact(AlarmManager.ELAPSED_REALTIME, 1500L, intent);
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(@NonNull Activity activity) {
        currentlyShowingDialog = true;
        String positiveButton = str("in_app_update_restart_button");
        String negativeButton = str("sign_in_cancel");
        new AlertDialog.Builder(activity).setMessage(str("pref_refresh_config"))
                .setPositiveButton(positiveButton, (dialog, id) -> {
                    reboot(activity);
                    currentlyShowingDialog = false;
                })
                .setNegativeButton(negativeButton, (dialog, id) -> {
                    currentlyShowingDialog = false;
                })
                .setOnDismissListener((dialog) -> {
                    currentlyShowingDialog = false;
                }).show();
    }

    private void showSettingUserDialogConfirmation(@NonNull Activity activity, SwitchPreference switchPref, SettingsEnum setting) {
        currentlyShowingDialog = true;
        new AlertDialog.Builder(activity)
                .setTitle(str("revanced_settings_confirm_user_dialog_title"))
                .setMessage(setting.userDialogMessage.toString())
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (setting.rebootApp) {
                        rebootDialog(activity);
                    } else {
                        currentlyShowingDialog = false;
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                    Boolean defaultBooleanValue = (Boolean) setting.defaultValue;
                    SettingsEnum.setValue(setting, defaultBooleanValue);
                    switchPref.setChecked(defaultBooleanValue);
                    currentlyShowingDialog = false;
                })
                .setOnDismissListener((dialog) -> {
                    currentlyShowingDialog = false;
                }).show();
    }
}
