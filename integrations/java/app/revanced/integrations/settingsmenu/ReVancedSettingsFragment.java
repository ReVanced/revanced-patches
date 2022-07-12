package app.revanced.integrations.settingsmenu;

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
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.ScreenSizeHelper;
import app.revanced.integrations.videoplayer.autorepeat.AutoRepeat;
import app.revanced.integrations.videoplayer.videourl.Copy;
import app.revanced.integrations.videoplayer.videourl.CopyWithTimeStamp;

public class ReVancedSettingsFragment extends PreferenceFragment {

    public static Context overlayContext;
    public static Class homeActivityClass;

    private SharedPreferences sharedPreferences;
    private PreferenceScreen adsSettingsPreferenceScreen;
    private PreferenceScreen bufferSettingsPreferenceScreen;
    private Preference codecDefault;
    private PreferenceScreen codecPreferenceScreen;
    private Preference codecVP9;
    private PreferenceScreen layoutSettingsPreferenceScreen;
    private PreferenceScreen miscsPreferenceScreen;
    private SwitchPreference tabletMiniplayer;
    private PreferenceScreen videoAdSettingsPreferenceScreen;
    private PreferenceScreen videoSettingsPreferenceScreen;
    private SwitchPreference vp9Override;
    private PreferenceScreen xSwipeControlPreferenceScreen;
    private boolean Registered = false;
    private boolean settingsInitialized = false;

    private final CharSequence[] videoQualityEntries = {"Auto", "144p", "240p", "360p", "480p", "720p", "1080p", "1440p", "2160p"};
    private final CharSequence[] videoQualityentryValues = {"-2", "144", "240", "360", "480", "720", "1080", "1440", "2160"};
    private final CharSequence[] videoSpeedEntries = {"Auto", "0.25x", "0.5x", "0.75x", "Normal", "1.25x", "1.5x", "1.75x", "2x", "3x", "4x", "5x"};
    private final CharSequence[] videoSpeedentryValues = {"-2", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0"};
    private final CharSequence[] buttonLocationEntries = {"None", "In player", "Under player", "Both"};
    private final CharSequence[] buttonLocationentryValues = {"NONE", "PLAYER", "BUTTON_BAR", "BOTH"};

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        if (str.equals(SettingsEnum.DEBUG_BOOLEAN.getPath())) {
            SettingsEnum.DEBUG_BOOLEAN.setValue(((SwitchPreference) findPreference(str)).isChecked());
        } else if (str.equals(SettingsEnum.HOME_ADS_SHOWN_BOOLEAN.getPath())) {
            SettingsEnum.HOME_ADS_SHOWN_BOOLEAN.setValue(((SwitchPreference) adsSettingsPreferenceScreen.findPreference(str)).isChecked());
            if (ReVancedUtils.getContext() != null && settingsInitialized) {
                rebootDialog(getActivity());
            }
        } else if (str.equals(SettingsEnum.VIDEO_ADS_SHOWN_BOOLEAN.getPath())) {
            SettingsEnum.VIDEO_ADS_SHOWN_BOOLEAN.setValue(((SwitchPreference) adsSettingsPreferenceScreen.findPreference(str)).isChecked());
            if (ReVancedUtils.getContext() != null && settingsInitialized) {
                rebootDialog(getActivity());
            }
        } else if (str.equals(SettingsEnum.REEL_BUTTON_SHOWN_BOOLEAN.getPath())) {
            SettingsEnum.REEL_BUTTON_SHOWN_BOOLEAN.setValue(((SwitchPreference) layoutSettingsPreferenceScreen.findPreference(str)).isChecked());
            if (ReVancedUtils.getContext() != null && settingsInitialized) {
                rebootDialog(getActivity());
            }
        } else if (str.equals(SettingsEnum.INFO_CARDS_SHOWN_BOOLEAN.getPath())) {
            SettingsEnum.INFO_CARDS_SHOWN_BOOLEAN.setValue(((SwitchPreference) layoutSettingsPreferenceScreen.findPreference(str)).isChecked());
        } else if (str.equals(SettingsEnum.BRANDING_SHOWN_BOOLEAN.getPath())) {
            SettingsEnum.BRANDING_SHOWN_BOOLEAN.setValue(((SwitchPreference) layoutSettingsPreferenceScreen.findPreference(str)).isChecked());
        } else if (str.equals(SettingsEnum.CAST_BUTTON_SHOWN_BOOLEAN.getPath())) {
            SettingsEnum.CAST_BUTTON_SHOWN_BOOLEAN.setValue(((SwitchPreference) layoutSettingsPreferenceScreen.findPreference(str)).isChecked());
        } else if (str.equals(SettingsEnum.USE_TABLET_MINIPLAYER_BOOLEAN.getPath())) {
            SettingsEnum.USE_TABLET_MINIPLAYER_BOOLEAN.setValue(((SwitchPreference) layoutSettingsPreferenceScreen.findPreference(str)).isChecked());
            if (ReVancedUtils.getContext() != null && settingsInitialized) {
                rebootDialog(getActivity());
            }
        } else if (str.equals(SettingsEnum.CREATE_BUTTON_SHOWN_BOOLEAN.getPath())) {
            SwitchPreference switchPreference = (SwitchPreference) layoutSettingsPreferenceScreen.findPreference(str);
            SettingsEnum.CREATE_BUTTON_SHOWN_BOOLEAN.setValue(switchPreference.isChecked());
            if (ReVancedUtils.getContext() != null && settingsInitialized) {
                rebootDialog(getActivity());
            }
        } else if (str.equals(SettingsEnum.USE_NEW_ACTIONBAR_BOOLEAN.getPath())) {
            SettingsEnum.USE_NEW_ACTIONBAR_BOOLEAN.setValue(((SwitchPreference) layoutSettingsPreferenceScreen.findPreference(str)).isChecked());
            if (ReVancedUtils.getContext() != null && settingsInitialized) {
                rebootDialog(getActivity());
            }
        } else if (str.equals(SettingsEnum.CODEC_OVERRIDE_BOOLEAN.getPath())) {
            SettingsEnum.CODEC_OVERRIDE_BOOLEAN.setValue(((SwitchPreference) findPreference(str)).isChecked());
            if (ReVancedUtils.getContext() != null && settingsInitialized) {
                rebootDialog(getActivity());
            }
        } else if (str.equals(SettingsEnum.PREFERRED_RESOLUTION_WIFI_INTEGER.getPath())) {
            ListPreference listPreference2 = (ListPreference) videoSettingsPreferenceScreen.findPreference(str);
            int index = SettingsEnum.PREFERRED_RESOLUTION_WIFI_INTEGER.getInt();
            listPreference2.setDefaultValue(index);
            listPreference2.setSummary(videoQualityEntries[listPreference2.findIndexOfValue(String.valueOf(index))]);
            SettingsEnum.PREFERRED_RESOLUTION_WIFI_INTEGER.setValue(index);
        } else if (str.equals(SettingsEnum.PREFERRED_RESOLUTION_MOBILE_INTEGER.getPath())) {
            ListPreference listPreference2 = (ListPreference) videoSettingsPreferenceScreen.findPreference(str);
            int index = SettingsEnum.PREFERRED_RESOLUTION_MOBILE_INTEGER.getInt();
            listPreference2.setDefaultValue(index);
            listPreference2.setSummary(videoQualityEntries[listPreference2.findIndexOfValue(String.valueOf(index))]);
            SettingsEnum.PREFERRED_RESOLUTION_MOBILE_INTEGER.setValue(index);
        } else if (str.equals(SettingsEnum.PREFERRED_VIDEO_SPEED_FLOAT.getPath())) {
            ListPreference listPreference4 = (ListPreference) videoSettingsPreferenceScreen.findPreference(str);
            Float value = SettingsEnum.PREFERRED_VIDEO_SPEED_FLOAT.getFloat();
            listPreference4.setDefaultValue(value);
            listPreference4.setSummary(videoSpeedEntries[listPreference4.findIndexOfValue(String.valueOf(value))]);
            SettingsEnum.PREFERRED_VIDEO_SPEED_FLOAT.setValue(value);
        } else if (str.equals(SettingsEnum.MAX_BUFFER_INTEGER.getPath())) {
            EditTextPreference editTextPreference3 = (EditTextPreference) bufferSettingsPreferenceScreen.findPreference(str);
            if (editTextPreference3 != null) {
                editTextPreference3.setSummary(editTextPreference3.getText());
                SettingsEnum.MAX_BUFFER_INTEGER.setValue(Integer.parseInt(editTextPreference3.getText()));
            }
        } else if (str.equals(SettingsEnum.PLAYBACK_MAX_BUFFER_INTEGER.getPath())) {
            EditTextPreference editTextPreference4 = (EditTextPreference) ReVancedSettingsFragment.this.bufferSettingsPreferenceScreen.findPreference(str);
            if (editTextPreference4 != null) {
                editTextPreference4.setSummary(editTextPreference4.getText());
                SettingsEnum.PLAYBACK_MAX_BUFFER_INTEGER.setValue(Integer.parseInt(editTextPreference4.getText()));
            }
        } else if (str.equals(SettingsEnum.MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER.getPath())) {
            EditTextPreference editTextPreference5 = (EditTextPreference) ReVancedSettingsFragment.this.bufferSettingsPreferenceScreen.findPreference(str);
            if (editTextPreference5 != null) {
                editTextPreference5.setSummary(editTextPreference5.getText());
                SettingsEnum.MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER.setValue(Integer.parseInt(editTextPreference5.getText()));
            }
        } else if (str.equals(SettingsEnum.USE_HDR_AUTO_BRIGHTNESS_BOOLEAN.getPath())) {
            SettingsEnum.USE_HDR_AUTO_BRIGHTNESS_BOOLEAN.setValue(((SwitchPreference) miscsPreferenceScreen.findPreference(str)).isChecked());
        } else if (str.equals(SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getPath())) {
            SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.setValue(((SwitchPreference) xSwipeControlPreferenceScreen.findPreference(str)).isChecked());
        } else if (str.equals(SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getPath())) {
            SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.setValue(((SwitchPreference) xSwipeControlPreferenceScreen.findPreference(str)).isChecked());
        } else if ("revanced_ryd_enabled".equals(str) && ReVancedUtils.getContext() != null && settingsInitialized) {
            rebootDialog(ReVancedSettingsFragment.this.getActivity());
        } else if (str.equals("pref_auto_repeat_button")) {
            AutoRepeatLinks();
        } else if ("pref_auto_repeat".equals(str)) {
            AutoRepeat.changeSelected(sharedPreferences.getBoolean("pref_auto_repeat", false), true);
        } else if ("pref_copy_video_url_timestamp_button_list".equals(str)) {
            CopyWithTimeStamp.refreshShouldBeShown();
        } else if ("pref_copy_video_url_button_list".equals(str)) {
            Copy.refreshShouldBeShown();
        }
    };

    @SuppressLint("ResourceType")
    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setSharedPreferencesName("youtube");
        try {
            int identifier = getResources().getIdentifier("revanced_prefs", "xml", getPackageName());

            addPreferencesFromResource(identifier);
            String stringByName = ReVancedUtils.getStringByName(getActivity(), "quality_auto");
            this.videoQualityEntries[0] = stringByName;
            this.videoSpeedEntries[0] = stringByName;
            String stringByName2 = ReVancedUtils.getStringByName(getActivity(), "pref_subtitles_scale_normal");
            if (stringByName2.equals("")) {
                this.videoSpeedEntries[4] = "Normal";
            } else {
                this.videoSpeedEntries[4] = stringByName2;
            }
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            this.sharedPreferences = sharedPreferences;
            this.settingsInitialized = sharedPreferences.getBoolean("revanced_initialized", false);
            this.sharedPreferences.registerOnSharedPreferenceChangeListener(this.listener);
            this.Registered = true;
            this.codecPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("codec_override");
            this.videoSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("video_settings");
            this.videoAdSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("video_ad_settings");
            this.adsSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("ad_settings");
            this.layoutSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("layout_settings");
            this.bufferSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("buffer_screen");
            this.miscsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("misc_screen");
            this.xSwipeControlPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("xfenster_screen");
            this.vp9Override = (SwitchPreference) this.codecPreferenceScreen.findPreference("revanced_vp9_enabled");
            this.codecDefault = this.codecPreferenceScreen.findPreference("pref_default_override");
            this.codecVP9 = this.codecPreferenceScreen.findPreference("pref_vp9_override");
            this.tabletMiniplayer = (SwitchPreference) this.layoutSettingsPreferenceScreen.findPreference("tablet_miniplayer");
            AutoRepeatLinks();
            final ListPreference listPreference = (ListPreference) this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_quality_wifi");
            final ListPreference listPreference2 = (ListPreference) this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_quality_mobile");
            setListPreferenceData(listPreference, true);
            setListPreferenceData(listPreference2, false);

            listPreference.setOnPreferenceClickListener(preference -> {
                ReVancedSettingsFragment.this.setListPreferenceData(listPreference, true);
                return false;
            });

            listPreference2.setOnPreferenceClickListener(preference -> {
                ReVancedSettingsFragment.this.setListPreferenceData(listPreference2, false);
                return false;
            });
            final ListPreference listPreference3 = (ListPreference) this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_speed");
            setSpeedListPreferenceData(listPreference3);

            listPreference3.setOnPreferenceClickListener(preference -> {
                ReVancedSettingsFragment.this.setSpeedListPreferenceData(listPreference3);
                return false;
            });
            Preference findPreference = findPreference("pref_about_field");

            this.codecDefault.setOnPreferenceClickListener(preference -> {
                SettingsEnum.CODEC_OVERRIDE_BOOLEAN.saveValue(false);
                return false;
            });

            this.codecVP9.setOnPreferenceClickListener(preference -> {
                SettingsEnum.CODEC_OVERRIDE_BOOLEAN.saveValue(true);
                return false;
            });

            if (ScreenSizeHelper.isTablet(ReVancedUtils.getContext())) {
                if (this.layoutSettingsPreferenceScreen.findPreference("tablet_miniplayer") != null) {
                    this.layoutSettingsPreferenceScreen.removePreference(this.tabletMiniplayer);
                }
            }


            this.sharedPreferences.edit().putBoolean("revanced_initialized", true);
            this.settingsInitialized = true;
        } catch (Throwable th) {
            LogHelper.printException(ReVancedSettingsFragment.class, "Unable to retrieve resourceId for xfile_prefs", th);
        }
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        if (this.Registered) {
            this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this.listener);
            this.Registered = false;
        }
        super.onDestroy();
    }

    protected void setListPreferenceData(ListPreference listPreference, boolean z) {
        listPreference.setEntries(this.videoQualityEntries);
        listPreference.setEntryValues(this.videoQualityentryValues);
        String string = this.sharedPreferences.getString(z ? "pref_preferred_video_quality_wifi" : "pref_preferred_video_quality_mobile", "-2");
        if (listPreference.getValue() == null) {
            listPreference.setValue(string);
        }
        listPreference.setSummary(this.videoQualityEntries[listPreference.findIndexOfValue(string)]);
    }

    protected void setSpeedListPreferenceData(ListPreference listPreference) {
        listPreference.setEntries(this.videoSpeedEntries);
        listPreference.setEntryValues(this.videoSpeedentryValues);
        String string = this.sharedPreferences.getString("pref_preferred_video_speed", "-2");
        if (listPreference.getValue() == null) {
            listPreference.setValue(string);
        }
        listPreference.setSummary(this.videoSpeedEntries[listPreference.findIndexOfValue(string)]);
    }

    protected void setCopyLinkListPreferenceData(ListPreference listPreference, String str) {
        listPreference.setEntries(this.buttonLocationEntries);
        listPreference.setEntryValues(this.buttonLocationentryValues);
        String string = this.sharedPreferences.getString(str, "NONE");
        if (listPreference.getValue() == null) {
            listPreference.setValue(string);
        }
        listPreference.setSummary(this.buttonLocationEntries[listPreference.findIndexOfValue(string)]);
    }

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

    private void AutoRepeatLinks() {
        boolean z = this.sharedPreferences.getBoolean("pref_auto_repeat_button", false);
        SwitchPreference switchPreference = (SwitchPreference) this.miscsPreferenceScreen.findPreference("pref_auto_repeat");
        if (switchPreference == null) {
            return;
        }
        if (z) {
            switchPreference.setEnabled(false);
            AutoRepeat.isAutoRepeatBtnEnabled = true;
            return;
        }
        switchPreference.setEnabled(true);
        AutoRepeat.isAutoRepeatBtnEnabled = false;
    }

    private void reboot(Activity activity, Class homeActivityClass) {
        int intent;
        intent = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        ((AlarmManager) activity.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.ELAPSED_REALTIME, 1500L, PendingIntent.getActivity(activity, 0, new Intent(activity, homeActivityClass), intent));
        Process.killProcess(Process.myPid());
    }

    private void rebootDialog(final Activity activity) {
        new AlertDialog.Builder(activity).setMessage(ReVancedUtils.getStringByName(activity, "pref_refresh_config")).setPositiveButton(ReVancedUtils.getStringByName(activity, "in_app_update_restart_button"), (dialog, id) -> reboot(activity, ReVancedSettingsFragment.homeActivityClass)).setNegativeButton(ReVancedUtils.getStringByName(activity, "sign_in_cancel"), null).show();
    }

}
