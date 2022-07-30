package app.revanced.integrations.settingsmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.videoplayer.AutoRepeat;

public class ReVancedSettingsFragment extends PreferenceFragment {

    public static Class homeActivityClass;

    private PreferenceScreen[] screens;

    private boolean Registered = false;
    private boolean settingsInitialized = false;

    private final CharSequence[] videoSpeedEntries = {"Auto", "0.25x", "0.5x", "0.75x", "Normal", "1.25x", "1.5x", "1.75x", "2x", "3x", "4x", "5x"};
    private final CharSequence[] videoSpeedentryValues = {"-2", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0"};
    //private final CharSequence[] buttonLocationEntries = {"None", "In player", "Under player", "Both"};
    //private final CharSequence[] buttonLocationentryValues = {"NONE", "PLAYER", "BUTTON_BAR", "BOTH"};

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        for (SettingsEnum setting : SettingsEnum.values()) {
            if (!setting.getPath().equals(str)) continue;
            Preference pref = this.findPreferenceOnScreen(str);

            LogHelper.debug(ReVancedSettingsFragment.class, "Setting " + setting.name() + " was changed. Preference " + str + ": " + pref.toString());

            if (pref instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) pref;
                setting.setValue(switchPref.isChecked());

                if (setting == SettingsEnum.PREFERRED_AUTO_REPEAT) {
                    AutoRepeat.changeSelected(setting.getBoolean(), true);
                }

            } else if (pref instanceof EditTextPreference) {
                EditTextPreference editPref = (EditTextPreference) pref;
                Object value = null;
                switch (setting.getReturnType()) {
                    case FLOAT:
                        value = Float.parseFloat(editPref.getText());
                        break;
                    case LONG:
                        value = Long.parseLong(editPref.getText());
                        break;
                    case STRING:
                        value = editPref.getText();
                        break;
                    case INTEGER:
                        value = Integer.parseInt(editPref.getText());
                        break;
                    default:
                        LogHelper.printException(ReVancedSettingsFragment.class, "Setting has no valid return type! " + setting.getReturnType());
                        break;
                }
                setting.setValue(value);
            } else if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                if (setting == SettingsEnum.PREFERRED_VIDEO_SPEED) {
                    Float value = SettingsEnum.PREFERRED_VIDEO_SPEED.getFloat();
                    listPref.setDefaultValue(value);
                    listPref.setSummary(videoSpeedEntries[listPref.findIndexOfValue(String.valueOf(value))]);
                    SettingsEnum.PREFERRED_VIDEO_SPEED.setValue(value);
                } else {
                    LogHelper.printException(ReVancedSettingsFragment.class, "No valid setting found: " + setting.toString());
                }
               /*
               if ("pref_copy_video_url_timestamp_button_list".equals(str)) {
                CopyWithTimeStamp.refreshShouldBeShown();
               } else if ("pref_copy_video_url_button_list".equals(str)) {
                Copy.refreshShouldBeShown();
               }
               */
            } else {
                LogHelper.printException(ReVancedSettingsFragment.class, "Setting cannot be handled! " + pref.toString());
            }

            if (ReVancedUtils.getContext() != null && settingsInitialized) {
                rebootDialog(getActivity());
            }
        }
    };

    @SuppressLint("ResourceType")
    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setSharedPreferencesName(SharedPrefHelper.SharedPrefNames.YOUTUBE.getName());
        try {
            int identifier = getResources().getIdentifier("revanced_prefs", "xml", getPackageName());
            addPreferencesFromResource(identifier);

            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            this.settingsInitialized = sharedPreferences.getBoolean("revanced_initialized", false);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this.listener);
            this.Registered = true;
            this.screens[0] = (PreferenceScreen) getPreferenceScreen().findPreference("codec_override");
            this.screens[1] = (PreferenceScreen) getPreferenceScreen().findPreference("video_settings");
            this.screens[2] = (PreferenceScreen) getPreferenceScreen().findPreference("video_ad_settings");
            this.screens[3] = (PreferenceScreen) getPreferenceScreen().findPreference("ad_settings");
            this.screens[4] = (PreferenceScreen) getPreferenceScreen().findPreference("layout_settings");
            this.screens[5] = (PreferenceScreen) getPreferenceScreen().findPreference("buffer_screen");
            this.screens[6] = (PreferenceScreen) getPreferenceScreen().findPreference("misc_screen");
            this.screens[7] = (PreferenceScreen) getPreferenceScreen().findPreference("swipe_screen");

            final ListPreference listPreference3 = (ListPreference) screens[1].findPreference("pref_preferred_video_speed");
            setSpeedListPreferenceData(listPreference3);

            listPreference3.setOnPreferenceClickListener(preference -> {
                setSpeedListPreferenceData(listPreference3);
                return false;
            });

            sharedPreferences.edit().putBoolean("revanced_initialized", true);
            this.settingsInitialized = true;
        } catch (Throwable th) {
            LogHelper.printException(ReVancedSettingsFragment.class, "Unable to retrieve resourceId for xfile_prefs", th);
        }
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        if (this.Registered) {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this.listener);
            this.Registered = false;
        }
        super.onDestroy();
    }

    private Preference findPreferenceOnScreen(CharSequence key) {
        if (key == null) {
            LogHelper.printException(ReVancedSettingsFragment.class, "Key cannot be null!");
            return null;
        }
        Preference pref = null;
        if (this.findPreference(key) != null) {
            pref = this.findPreference(key);
        } else {
            for (PreferenceScreen screen : this.screens) {
                Preference toCheck = screen.findPreference(key);
                if (toCheck == null) continue;
                pref = toCheck;
                LogHelper.debug(ReVancedSettingsFragment.class, "Found preference " + key + " on screen: " + screen.getTitle());
            }
        }

        return pref;
    }

    private void setSpeedListPreferenceData(ListPreference listPreference) {
        listPreference.setEntries(this.videoSpeedEntries);
        listPreference.setEntryValues(this.videoSpeedentryValues);
    }

    /*
    private void setCopyLinkListPreferenceData(ListPreference listPreference, String str) {
        listPreference.setEntries(this.buttonLocationEntries);
        listPreference.setEntryValues(this.buttonLocationentryValues);
        String string = this.sharedPreferences.getString(str, "NONE");
        if (listPreference.getValue() == null) {
            listPreference.setValue(string);
        }
        listPreference.setSummary(this.buttonLocationEntries[listPreference.findIndexOfValue(string)]);
    }
    */

    private String getPackageName() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException(ReVancedSettingsFragment.class, "Context is null, returning com.google.android.youtube!");
            return "com.google.android.youtube";
        }
        String PACKAGE_NAME = context.getPackageName();
        LogHelper.debug(ReVancedSettingsFragment.class, "getPackageName: " + PACKAGE_NAME);

        return PACKAGE_NAME;
    }

    private void reboot(Activity activity, Class homeActivityClass) {
        int intent;
        intent = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        ((AlarmManager) activity.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.ELAPSED_REALTIME, 1500L, PendingIntent.getActivity(activity, 0, new Intent(activity, homeActivityClass), intent));
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(final Activity activity) {
        new AlertDialog.Builder(activity).setMessage(getStringByName(activity, "pref_refresh_config")).setPositiveButton(getStringByName(activity, "in_app_update_restart_button"), (dialog, id) -> reboot(activity, ReVancedSettingsFragment.homeActivityClass)).setNegativeButton(getStringByName(activity, "sign_in_cancel"), null).show();
    }

    private String getStringByName(Context context, String name) {
        try {
            Resources res = context.getResources();
            return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
        } catch (Throwable exception) {
            LogHelper.printException(ReVancedUtils.class, "Resource not found.", exception);
            return "";
        }
    }

}
