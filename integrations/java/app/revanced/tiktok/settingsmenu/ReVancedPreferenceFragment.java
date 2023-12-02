package app.revanced.tiktok.settingsmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.shared.settings.SettingsUtils;
import app.revanced.tiktok.settings.SettingsEnum;
import app.revanced.tiktok.settings.SharedPrefCategory;
import app.revanced.tiktok.settingsmenu.preference.DownloadPathPreference;
import app.revanced.tiktok.settingsmenu.preference.RangeValuePreference;
import app.revanced.tiktok.settingsmenu.preference.categories.DownloadsPreferenceCategory;
import app.revanced.tiktok.settingsmenu.preference.categories.FeedFilterPreferenceCategory;
import app.revanced.tiktok.settingsmenu.preference.categories.IntegrationsPreferenceCategory;
import app.revanced.tiktok.settingsmenu.preference.categories.SimSpoofPreferenceCategory;
import app.revanced.tiktok.utils.ReVancedUtils;

@SuppressWarnings("deprecation")
public class ReVancedPreferenceFragment extends PreferenceFragment {
    private boolean registered = false;
    private boolean settingsInitialized = false;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        try {
            SettingsEnum setting = SettingsEnum.getSettingsFromPath(str);
            if (setting == null) {
                return;
            }
            Preference pref = findPreference(str);
            if (pref == null) {
                return;
            }
            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                SettingsEnum.setValue(setting, switchPref.isChecked());
            } else if (pref instanceof EditTextPreference) {
                EditTextPreference editPreference = (EditTextPreference) pref;
                SettingsEnum.setValue(setting, editPreference.getText());
            } else if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                SettingsEnum.setValue(setting, listPref.getValue());
                updateListPreferenceSummary((ListPreference) pref, setting);
            } else if (pref instanceof RangeValuePreference) {
                RangeValuePreference rangeValuePref = (RangeValuePreference) pref;
                SettingsEnum.setValue(setting, rangeValuePref.getValue());
            } else if (pref instanceof DownloadPathPreference) {
                DownloadPathPreference downloadPathPref = (DownloadPathPreference) pref;
                SettingsEnum.setValue(setting, downloadPathPref.getValue());
            } else {
                LogHelper.printException(() -> "Setting cannot be handled: " + pref.getClass() + " " + pref);
                return;
            }
            if (ReVancedUtils.getAppContext() != null && this.settingsInitialized && setting.rebootApp) {
                rebootDialog(getActivity());
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "OnSharedPreferenceChangeListener failure", ex);
        }
    };

    private void updateListPreferenceSummary(ListPreference listPreference, SettingsEnum setting) {
        String objectStringValue = setting.getObjectValue().toString();
        final int entryIndex = listPreference.findIndexOfValue(objectStringValue);
        if (entryIndex >= 0) {
            listPreference.setSummary(listPreference.getEntries()[entryIndex]);
            listPreference.setValue(objectStringValue);
        } else {
            listPreference.setSummary(objectStringValue);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registered = true;

        getPreferenceManager().setSharedPreferencesName(SharedPrefCategory.TIKTOK_PREFS.prefName);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this.listener);

        final Activity context = this.getActivity();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        new FeedFilterPreferenceCategory(context, preferenceScreen);
        new DownloadsPreferenceCategory(context, preferenceScreen);
        new SimSpoofPreferenceCategory(context, preferenceScreen);
        new IntegrationsPreferenceCategory(context, preferenceScreen);

        this.settingsInitialized = true;
    }

    @Override
    public void onDestroy() {
        if (this.registered) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
            this.registered = false;
        }

        super.onDestroy();
    }

    private void rebootDialog(@NonNull Context context) {
        new AlertDialog.Builder(context).setMessage("Refresh and restart")
                .setPositiveButton("Restart", (dialog, i) -> SettingsUtils.restartApp(context))
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
