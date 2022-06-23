package app.revanced.integrations.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.videoplayer.autorepeat.AutoRepeat;
import app.revanced.integrations.videoswipecontrols.FensterGestureListener;
import app.revanced.integrations.videoplayer.settings.XReboot;
import app.revanced.integrations.utils.ScreenSizeHelper;
import app.revanced.integrations.utils.SwipeHelper;
import app.revanced.integrations.videoplayer.videourl.Copy;
import app.revanced.integrations.videoplayer.videourl.CopyWithTimeStamp;
import app.revanced.integrations.BuildConfig;

/* loaded from: classes6.dex */
public class XSettingsFragment extends PreferenceFragment {

    // YouTubePlayerOverlaysLayout.overlayContext
    public static Context overlayContext;
    // Shell_HomeActivity.class
    public static Class homeActivityClass;
    private Toast toast;
    private PreferenceScreen adsSettingsPreferenceScreen;
    private PreferenceScreen bufferSettingsPreferenceScreen;
    private Preference codecDefault;
    private Preference codecHDRH;
    private Preference codecHDRS;
    private PreferenceScreen codecPreferenceScreen;
    private Preference codecVP9;
    private PreferenceScreen layoutSettingsPreferenceScreen;
    private EditTextPreference manufacturerOverride;
    private PreferenceScreen miscsPreferenceScreen;
    private EditTextPreference modelOverride;
    public SharedPreferences sharedPreferences;
    private SwitchPreference tabletComments;
    private SwitchPreference tabletMiniplayer;
    private PreferenceScreen videoAdSettingsPreferenceScreen;
    private PreferenceScreen videoSettingsPreferenceScreen;
    private SwitchPreference vp9Override;
    private PreferenceScreen xFensterPreferenceScreen;
    private boolean Registered = false;
    CharSequence[] videoQualityEntries = {"Auto", "144p", "240p", "360p", "480p", "720p", "1080p", "1440p", "2160p"};
    CharSequence[] videoQualityentryValues = {"-2", "144", "240", "360", "480", "720", "1080", "1440", "2160"};
    CharSequence[] minimizedVideoEntries = {"Auto", "Video only", "Video with controls"};
    CharSequence[] minimizedVideoentryValues = {"-2", "0", "1"};
    CharSequence[] videoSpeedEntries = {"Auto", "0.25x", "0.5x", "0.75x", "Normal", "1.25x", "1.5x", "1.75x", "2x"};
    CharSequence[] videoSpeedentryValues = {"-2", "0.25", "0.5", "0.75", BuildConfig.VERSION_NAME, "1.25", "1.5", "1.75", "2.0"};
    CharSequence[] buttonLocationEntries = {"None", "In player", "Under player", "Both"};
    CharSequence[] buttonLocationentryValues = {"NONE", "PLAYER", "BUTTON_BAR", "BOTH"};
    private long PreviousClick = 0;
    private int clicks = 0;
    private final int neededClicks = 5;
    private boolean hiddenMenuOpened = false;
    private boolean settingsInitialized = false;
    // from class: app.revanced.integrations.settings.XSettingsFragment.9
// android.content.SharedPreferences.OnSharedPreferenceChangeListener
    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, str) -> {
        if ("debug_xfile_enabled".equals(str)) {
            SettingsEnum.DEBUG_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.findPreference("debug_xfile_enabled")).isChecked());
        } else if ("vp9_xfile_enabled".equals(str)) {
            if (((SwitchPreference) XSettingsFragment.this.codecPreferenceScreen.findPreference("vp9_xfile_enabled")).isChecked()) {
                sharedPreferences.edit().putString("override_manufacturer", "samsung").apply();
                sharedPreferences.edit().putString("override_model", "SM-G920F").apply();
                SettingsEnum.MANUFACTURER_OVERRIDE_STRING.setValue("samsung");
                SettingsEnum.MODEL_OVERRIDE_STRING.setValue("SM-G920F");
                return;
            }
            sharedPreferences.edit().remove("override_manufacturer").apply();
            sharedPreferences.edit().remove("override_model").apply();
            SettingsEnum.MANUFACTURER_OVERRIDE_STRING.setValue(null);
            SettingsEnum.MODEL_OVERRIDE_STRING.setValue(null);
        } else if ("override_manufacturer".equals(str)) {
            EditTextPreference editTextPreference = (EditTextPreference) XSettingsFragment.this.codecPreferenceScreen.findPreference("override_manufacturer");
            if (editTextPreference != null) {
                editTextPreference.setSummary(editTextPreference.getText());
                SettingsEnum.MANUFACTURER_OVERRIDE_STRING.setValue(editTextPreference.getText());
            }
        } else if ("override_model".equals(str)) {
            EditTextPreference editTextPreference2 = (EditTextPreference) XSettingsFragment.this.codecPreferenceScreen.findPreference("override_model");
            if (editTextPreference2 != null) {
                editTextPreference2.setSummary(editTextPreference2.getText());
                SettingsEnum.MODEL_OVERRIDE_STRING.setValue(editTextPreference2.getText());
            }
        } else if ("home_ads_enabled".equals(str)) {
            SettingsEnum.HOME_ADS_SHOWN_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.adsSettingsPreferenceScreen.findPreference("home_ads_enabled")).isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("video_ads_enabled".equals(str)) {
            SettingsEnum.VIDEO_ADS_SHOWN_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.adsSettingsPreferenceScreen.findPreference("video_ads_enabled")).isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("reel_enabled".equals(str)) {
            SettingsEnum.REEL_BUTTON_SHOWN_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("reel_enabled")).isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("info_card_suggestions_enabled".equals(str)) {
            SettingsEnum.SUGGESTIONS_SHOWN_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("info_card_suggestions_enabled")).isChecked());
        } else if ("info_cards_enabled".equals(str)) {
            SettingsEnum.INFO_CARDS_SHOWN_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("info_cards_enabled")).isChecked());
        } else if ("branding_watermark_enabled".equals(str)) {
            SettingsEnum.BRANDING_SHOWN_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("branding_watermark_enabled")).isChecked());
        } else if ("cast_button_enabled".equals(str)) {
            SettingsEnum.CAST_BUTTON_SHOWN_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("cast_button_enabled")).isChecked());
        } else if ("tablet_miniplayer".equals(str)) {
            SettingsEnum.USE_TABLET_MINIPLAYER_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("tablet_miniplayer")).isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("comments_location".equals(str)) {
            SwitchPreference switchPreference = (SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("comments_location");
            SettingsEnum.CHANGE_COMMENT_LOCATION_BOOLEAN.setValue(switchPreference.isChecked());
            SwipeHelper.isTabletMode = switchPreference.isChecked();
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("xfile_create_button_hidden".equals(str)) {
            SwitchPreference switchPreference = (SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("xfile_create_button_hidden");
            SettingsEnum.CREATE_BUTTON_SHOWN_BOOLEAN.setValue(switchPreference.isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("xfile_new_actionbar".equals(str)) {
            SettingsEnum.USE_NEW_ACTIONBAR_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("xfile_new_actionbar")).isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("xfile_zoom_to_fit_vertical".equals(str)) {
            SettingsEnum.USE_VERTICAL_ZOOM_TO_FIT_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("xfile_zoom_to_fit_vertical")).isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("pref_minimized_video_preview".equals(str)) {
            ListPreference listPreference = (ListPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("pref_minimized_video_preview");
            String string = sharedPreferences.getString("pref_minimized_video_preview", "-2");
            listPreference.setDefaultValue(string);
            listPreference.setSummary(XSettingsFragment.this.minimizedVideoEntries[listPreference.findIndexOfValue(string)]);
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("xfile_accessibility_seek_buttons".equals(str)) {
            SettingsEnum.ACCESSIBILITY_SEEK_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.layoutSettingsPreferenceScreen.findPreference("xfile_accessibility_seek_buttons")).isChecked());
        } else if ("override_resolution_xfile_enabled".equals(str)) {
            SettingsEnum.CODEC_OVERRIDE_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.findPreference("override_resolution_xfile_enabled")).isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("pref_auto_captions".equals(str)) {
            SettingsEnum.PREFERRED_AUTO_CAPTIONS_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.findPreference("pref_auto_captions")).isChecked());
            if (Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
                XReboot.RebootDialog(XSettingsFragment.this.getActivity());
            }
        } else if ("pref_preferred_video_quality_wifi".equals(str)) {
            ListPreference listPreference2 = (ListPreference) XSettingsFragment.this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_quality_wifi");
            String string2 = sharedPreferences.getString("pref_preferred_video_quality_wifi", "-2");
            listPreference2.setDefaultValue(string2);
            listPreference2.setSummary(XSettingsFragment.this.videoQualityEntries[listPreference2.findIndexOfValue(string2)]);
            SettingsEnum.PREFERRED_RESOLUTION_WIFI_INTEGER.setValue(Integer.parseInt(string2));
        } else if ("pref_preferred_video_quality_mobile".equals(str)) {
            ListPreference listPreference3 = (ListPreference) XSettingsFragment.this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_quality_mobile");
            String string3 = sharedPreferences.getString("pref_preferred_video_quality_mobile", "-2");
            listPreference3.setDefaultValue(string3);
            listPreference3.setSummary(XSettingsFragment.this.videoQualityEntries[listPreference3.findIndexOfValue(string3)]);
            SettingsEnum.PREFERRED_RESOLUTION_MOBILE_INTEGER.setValue(Integer.parseInt(string3));
        } else if ("pref_preferred_video_speed".equals(str)) {
            ListPreference listPreference4 = (ListPreference) XSettingsFragment.this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_speed");
            String string4 = sharedPreferences.getString("pref_preferred_video_speed", "-2");
            listPreference4.setDefaultValue(string4);
            listPreference4.setSummary(XSettingsFragment.this.videoSpeedEntries[listPreference4.findIndexOfValue(string4)]);
            SettingsEnum.PREFERRED_VIDEO_SPEED_FLOAT.setValue(Float.parseFloat(string4));
        } else if ("pref_max_buffer_ms".equals(str)) {
            EditTextPreference editTextPreference3 = (EditTextPreference) XSettingsFragment.this.bufferSettingsPreferenceScreen.findPreference("pref_max_buffer_ms");
            if (editTextPreference3 != null) {
                editTextPreference3.setSummary(editTextPreference3.getText());
                SettingsEnum.MAX_BUFFER_INTEGER.setValue(Integer.parseInt(editTextPreference3.getText()));
            }
        } else if ("pref_buffer_for_playback_ms".equals(str)) {
            EditTextPreference editTextPreference4 = (EditTextPreference) XSettingsFragment.this.bufferSettingsPreferenceScreen.findPreference("pref_buffer_for_playback_ms");
            if (editTextPreference4 != null) {
                editTextPreference4.setSummary(editTextPreference4.getText());
                SettingsEnum.PLAYBACK_MAX_BUFFER_INTEGER.setValue(Integer.parseInt(editTextPreference4.getText()));
            }
        } else if ("pref_buffer_for_playback_after_rebuffer_ms".equals(str)) {
            EditTextPreference editTextPreference5 = (EditTextPreference) XSettingsFragment.this.bufferSettingsPreferenceScreen.findPreference("pref_buffer_for_playback_after_rebuffer_ms");
            if (editTextPreference5 != null) {
                editTextPreference5.setSummary(editTextPreference5.getText());
                SettingsEnum.MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER.setValue(Integer.parseInt(editTextPreference5.getText()));
            }
        } else if ("pref_auto_repeat_button".equals(str)) {
            XSettingsFragment.this.AutoRepeatLinks();
        } else if ("pref_auto_repeat".equals(str)) {
            AutoRepeat.changeSelected(sharedPreferences.getBoolean("pref_auto_repeat", false), true);
        } else if ("pref_copy_video_url_timestamp_button_list".equals(str)) {
            CopyWithTimeStamp.refreshShouldBeShown();
        } else if ("pref_copy_video_url_button_list".equals(str)) {
            Copy.refreshShouldBeShown();
        } else if ("pref_hdr_autobrightness".equals(str)) {
            SettingsEnum.USE_HDR_BRIGHTNESS_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.miscsPreferenceScreen.findPreference("pref_hdr_autobrightness")).isChecked());
        } else if ("pref_xfenster_brightness".equals(str)) {
            SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.xFensterPreferenceScreen.findPreference("pref_xfenster_brightness")).isChecked());
        } else if ("pref_xfenster_volume".equals(str)) {
            SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.setValue(((SwitchPreference) XSettingsFragment.this.xFensterPreferenceScreen.findPreference("pref_xfenster_volume")).isChecked());
        } else if ("pref_xfenster_tablet".equals(str)) {
            SwipeHelper.isTabletMode = ((SwitchPreference) XSettingsFragment.this.xFensterPreferenceScreen.findPreference("pref_xfenster_tablet")).isChecked();
        } else if ("pref_xfenster_swipe_threshold".equals(str)) {
            EditTextPreference editTextPreference6 = (EditTextPreference) XSettingsFragment.this.xFensterPreferenceScreen.findPreference("pref_xfenster_swipe_threshold");
            if (editTextPreference6 != null) {
                editTextPreference6.setSummary(editTextPreference6.getText());
                try {
                    FensterGestureListener.SWIPE_THRESHOLD = Integer.parseInt(editTextPreference6.getText());
                } catch (NumberFormatException unused) {
                    FensterGestureListener.SWIPE_THRESHOLD = 0;
                }
            }
        } else if ("pref_xfenster_swipe_padding_top".equals(str)) {
            EditTextPreference editTextPreference7 = (EditTextPreference) XSettingsFragment.this.xFensterPreferenceScreen.findPreference("pref_xfenster_swipe_padding_top");
            if (editTextPreference7 != null) {
                editTextPreference7.setSummary(editTextPreference7.getText());
                try {
                    FensterGestureListener.TOP_PADDING = Integer.parseInt(editTextPreference7.getText());
                } catch (NumberFormatException unused2) {
                    FensterGestureListener.TOP_PADDING = 20;
                }
            }
        } else if ("vanced_ryd_enabled".equals(str) && Settings.getContext() != null && XSettingsFragment.this.settingsInitialized) {
            XReboot.RebootDialog(XSettingsFragment.this.getActivity());
        }
    };

    static /* synthetic */ int access$308(XSettingsFragment xSettingsFragment) {
        int i = xSettingsFragment.clicks;
        xSettingsFragment.clicks = i + 1;
        return i;
    }

    @SuppressLint("ResourceType")
    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setSharedPreferencesName("youtube");
        try {
            int identifier = getResources().getIdentifier("xfile_prefs", "xml", Settings.getPackageName());

            addPreferencesFromResource(identifier);
            String stringByName = Settings.getStringByName(getActivity(), "quality_auto");
            this.videoQualityEntries[0] = stringByName;
            this.minimizedVideoEntries[0] = stringByName;
            this.videoSpeedEntries[0] = stringByName;
            String stringByName2 = Settings.getStringByName(getActivity(), "pref_subtitles_scale_normal");
            if (stringByName2.equals("")) {
                this.videoSpeedEntries[4] = "Normal";
            } else {
                this.videoSpeedEntries[4] = stringByName2;
            }
            this.minimizedVideoEntries[1] = Settings.getStringByName(getActivity(), "xfile_miniplayer_style_video");
            this.minimizedVideoEntries[2] = Settings.getStringByName(getActivity(), "xfile_miniplayer_style_video_controls");
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            this.sharedPreferences = sharedPreferences;
            this.settingsInitialized = sharedPreferences.getBoolean("xfile_initialized", false);
            this.sharedPreferences.registerOnSharedPreferenceChangeListener(this.listener);
            this.Registered = true;
            this.hiddenMenuOpened = this.sharedPreferences.getBoolean("xfile_hiddenMenu_enabled", false);
            this.codecPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("codec_override");
            this.videoSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("video_settings");
            this.videoAdSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("video_ad_settings");
            this.adsSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("ad_settings");
            this.layoutSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("layout_settings");
            this.bufferSettingsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("buffer_screen");
            this.miscsPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("misc_screen");
            this.xFensterPreferenceScreen = (PreferenceScreen) getPreferenceScreen().findPreference("xfenster_screen");
            this.vp9Override = (SwitchPreference) this.codecPreferenceScreen.findPreference("vp9_xfile_enabled");
            this.manufacturerOverride = (EditTextPreference) this.codecPreferenceScreen.findPreference("override_manufacturer");
            this.modelOverride = (EditTextPreference) this.codecPreferenceScreen.findPreference("override_model");
            this.codecDefault = this.codecPreferenceScreen.findPreference("pref_default_override");
            this.codecVP9 = this.codecPreferenceScreen.findPreference("pref_vp9_override");
            this.codecHDRH = this.codecPreferenceScreen.findPreference("pref_hdrhardware_override");
            this.codecHDRS = this.codecPreferenceScreen.findPreference("pref_hdrsoftware_override");
            this.tabletMiniplayer = (SwitchPreference) this.layoutSettingsPreferenceScreen.findPreference("tablet_miniplayer");
            this.tabletComments = (SwitchPreference) this.layoutSettingsPreferenceScreen.findPreference("comments_location");
            AutoRepeatLinks();
            EditTextPreference editTextPreference = this.manufacturerOverride;
            editTextPreference.setSummary(editTextPreference.getText());
            EditTextPreference editTextPreference2 = this.modelOverride;
            editTextPreference2.setSummary(editTextPreference2.getText());
            CheckHiddenMenuStatus();
            final ListPreference listPreference = (ListPreference) this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_quality_wifi");
            final ListPreference listPreference2 = (ListPreference) this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_quality_mobile");
            setListPreferenceData(listPreference, true);
            setListPreferenceData(listPreference2, false);
            // from class: app.revanced.integrations.settings.XSettingsFragment.1
// android.preference.Preference.OnPreferenceClickListener
            listPreference.setOnPreferenceClickListener(preference -> {
                XSettingsFragment.this.setListPreferenceData(listPreference, true);
                return false;
            });
            // from class: app.revanced.integrations.settings.XSettingsFragment.2
// android.preference.Preference.OnPreferenceClickListener
            listPreference2.setOnPreferenceClickListener(preference -> {
                XSettingsFragment.this.setListPreferenceData(listPreference2, false);
                return false;
            });
            final ListPreference listPreference3 = (ListPreference) this.videoSettingsPreferenceScreen.findPreference("pref_preferred_video_speed");
            setSpeedListPreferenceData(listPreference3);
            // from class: app.revanced.integrations.settings.XSettingsFragment.3
// android.preference.Preference.OnPreferenceClickListener
            listPreference3.setOnPreferenceClickListener(preference -> {
                XSettingsFragment.this.setSpeedListPreferenceData(listPreference3);
                return false;
            });
            Preference findPreference = findPreference("pref_about_field");
            final String stringByName3 = Settings.getStringByName(getActivity(), "xfile_hiddenmenu_open");
            final String stringByName4 = Settings.getStringByName(getActivity(), "xfile_hiddenmenu_opened");
            final String str = " " + Settings.getStringByName(getActivity(), "xfile_hiddenmenu_needed");
            // from class: app.revanced.integrations.settings.XSettingsFragment.4
// android.preference.Preference.OnPreferenceClickListener
            findPreference.setOnPreferenceClickListener(preference -> {
                if (XSettingsFragment.this.hiddenMenuOpened) {
                    if (XSettingsFragment.this.toast != null) {
                        XSettingsFragment.this.toast.cancel();
                    }
                    XSettingsFragment.this.toast = Toast.makeText(Settings.getContext(), stringByName3, Toast.LENGTH_SHORT);
                    XSettingsFragment.this.toast.show();
                    return false;
                }
                long currentTimeMillis = System.currentTimeMillis() - XSettingsFragment.this.PreviousClick;
                XSettingsFragment.this.PreviousClick = System.currentTimeMillis();
                if (currentTimeMillis / 1000 < 2) {
                    XSettingsFragment.access$308(XSettingsFragment.this);
                    int i = XSettingsFragment.this.neededClicks - XSettingsFragment.this.clicks;
                    if (XSettingsFragment.this.toast != null) {
                        XSettingsFragment.this.toast.cancel();
                    }
                    if (i <= 0) {
                        XSettingsFragment.this.toast = Toast.makeText(Settings.getContext(), stringByName4, Toast.LENGTH_SHORT);
                        XSettingsFragment.this.hiddenMenuOpened = true;
                        XSettingsFragment.this.sharedPreferences.edit().putBoolean("xfile_hiddenMenu_enabled", true).apply();
                        XSettingsFragment.this.CheckHiddenMenuStatus();
                    } else {
                        XSettingsFragment xSettingsFragment = XSettingsFragment.this;
                        Context context = Settings.getContext();
                        xSettingsFragment.toast = Toast.makeText(context, i + str, Toast.LENGTH_SHORT);
                    }
                    XSettingsFragment.this.toast.show();
                } else {
                    XSettingsFragment.this.clicks = 0;
                }
                return false;
            });
            // from class: app.revanced.integrations.settings.XSettingsFragment.5
// android.preference.Preference.OnPreferenceClickListener
            this.codecDefault.setOnPreferenceClickListener(preference -> {
                XSettingsFragment.this.ChangeCodec(preference);
                return false;
            });
            // from class: app.revanced.integrations.settings.XSettingsFragment.6
// android.preference.Preference.OnPreferenceClickListener
            this.codecVP9.setOnPreferenceClickListener(preference -> {
                XSettingsFragment.this.ChangeCodec(preference);
                return false;
            });
            // from class: app.revanced.integrations.settings.XSettingsFragment.7
// android.preference.Preference.OnPreferenceClickListener
            this.codecHDRH.setOnPreferenceClickListener(preference -> {
                XSettingsFragment.this.ChangeCodec(preference);
                return false;
            });
            // from class: app.revanced.integrations.settings.XSettingsFragment.8
// android.preference.Preference.OnPreferenceClickListener
            this.codecHDRS.setOnPreferenceClickListener(preference -> {
                XSettingsFragment.this.ChangeCodec(preference);
                return false;
            });
            if (ScreenSizeHelper.isTablet(YouTubeTikTokRoot_Application.getAppContext())) {
                if (this.layoutSettingsPreferenceScreen.findPreference("tablet_miniplayer") != null) {
                    this.layoutSettingsPreferenceScreen.removePreference(this.tabletMiniplayer);
                }
                if (this.layoutSettingsPreferenceScreen.findPreference("comments_location") != null) {
                    this.layoutSettingsPreferenceScreen.removePreference(this.tabletComments);
                }
            }
            this.sharedPreferences.edit().putBoolean("xfile_initialized", true);
            this.settingsInitialized = true;
        } catch (Throwable th) {
            LogHelper.printException("XSettingsFragment", "Unable to retrieve resourceId for xfile_prefs", th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void ChangeCodec(Preference preference) {
        String key = preference.getKey();
        char c = 65535;
        switch (key.hashCode()) {
            case -1420246871:
                if (key.equals("pref_hdrhardware_override")) {
                    c = 0;
                    break;
                }
                break;
            case -350518296:
                if (key.equals("pref_vp9_override")) {
                    c = 1;
                    break;
                }
                break;
            case 1613958090:
                if (key.equals("pref_hdrsoftware_override")) {
                    c = 2;
                    break;
                }
                break;
        }
        String str = "samsung";
        String str2 = null;
        switch (c) {
            case 0:
                str2 = "SM-G955W";
                break;
            case 1:
                str2 = "SM-G920F";
                break;
            case 2:
                str = "Google";
                str2 = "Pixel XL";
                break;
            default:
                str = null;
                break;
        }
        if (str != null) {
            this.sharedPreferences.edit().putString("override_manufacturer", str).apply();
        } else {
            this.sharedPreferences.edit().remove("override_manufacturer").apply();
        }
        if (str2 != null) {
            this.sharedPreferences.edit().putString("override_model", str2).apply();
        } else {
            this.sharedPreferences.edit().remove("override_model").apply();
        }
        this.manufacturerOverride.setText(str);
        this.modelOverride.setText(str2);
        EditTextPreference editTextPreference = this.manufacturerOverride;
        editTextPreference.setSummary(editTextPreference.getText());
        EditTextPreference editTextPreference2 = this.modelOverride;
        editTextPreference2.setSummary(editTextPreference2.getText());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void CheckHiddenMenuStatus() {
        if (this.hiddenMenuOpened) {
            if (this.codecPreferenceScreen.findPreference("vp9_xfile_enabled") != null) {
                this.codecPreferenceScreen.removePreference(this.vp9Override);
            }
            if (this.codecPreferenceScreen.findPreference("override_manufacturer") == null) {
                this.codecPreferenceScreen.addPreference(this.manufacturerOverride);
                EditTextPreference editTextPreference = this.manufacturerOverride;
                editTextPreference.setSummary(editTextPreference.getText());
            }
            if (this.codecPreferenceScreen.findPreference("override_model") == null) {
                this.codecPreferenceScreen.addPreference(this.modelOverride);
                EditTextPreference editTextPreference2 = this.modelOverride;
                editTextPreference2.setSummary(editTextPreference2.getText());
            }
            if (this.codecPreferenceScreen.findPreference("pref_default_override") == null) {
                this.codecPreferenceScreen.addPreference(this.codecDefault);
                // from class: app.revanced.integrations.settings.XSettingsFragment.10
// android.preference.Preference.OnPreferenceClickListener
                this.codecDefault.setOnPreferenceClickListener(preference -> {
                    XSettingsFragment.this.ChangeCodec(preference);
                    return false;
                });
            }
            if (this.codecPreferenceScreen.findPreference("pref_vp9_override") == null) {
                this.codecPreferenceScreen.addPreference(this.codecVP9);
                // from class: app.revanced.integrations.settings.XSettingsFragment.11
// android.preference.Preference.OnPreferenceClickListener
                this.codecVP9.setOnPreferenceClickListener(preference -> {
                    XSettingsFragment.this.ChangeCodec(preference);
                    return false;
                });
            }
            if (this.codecPreferenceScreen.findPreference("pref_hdrhardware_override") == null) {
                this.codecPreferenceScreen.addPreference(this.codecHDRH);
                // from class: app.revanced.integrations.settings.XSettingsFragment.12
// android.preference.Preference.OnPreferenceClickListener
                this.codecHDRH.setOnPreferenceClickListener(preference -> {
                    XSettingsFragment.this.ChangeCodec(preference);
                    return false;
                });
            }
            if (this.codecPreferenceScreen.findPreference("pref_hdrsoftware_override") == null) {
                this.codecPreferenceScreen.addPreference(this.codecHDRS);
                // from class: app.revanced.integrations.settings.XSettingsFragment.13
// android.preference.Preference.OnPreferenceClickListener
                this.codecHDRS.setOnPreferenceClickListener(preference -> {
                    XSettingsFragment.this.ChangeCodec(preference);
                    return false;
                });
                return;
            }
            return;
        }
        if (this.codecPreferenceScreen.findPreference("vp9_xfile_enabled") == null) {
            this.codecPreferenceScreen.addPreference(this.vp9Override);
        }
        if (this.codecPreferenceScreen.findPreference("override_manufacturer") != null) {
            this.codecPreferenceScreen.removePreference(this.manufacturerOverride);
        }
        if (this.codecPreferenceScreen.findPreference("override_model") != null) {
            this.codecPreferenceScreen.removePreference(this.modelOverride);
        }
        if (this.codecPreferenceScreen.findPreference("pref_default_override") != null) {
            this.codecPreferenceScreen.removePreference(this.codecDefault);
        }
        if (this.codecPreferenceScreen.findPreference("pref_vp9_override") != null) {
            this.codecPreferenceScreen.removePreference(this.codecVP9);
        }
        if (this.codecPreferenceScreen.findPreference("pref_hdrhardware_override") != null) {
            this.codecPreferenceScreen.removePreference(this.codecHDRH);
        }
        if (this.codecPreferenceScreen.findPreference("pref_hdrsoftware_override") != null) {
            this.codecPreferenceScreen.removePreference(this.codecHDRS);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void AutoRepeatLinks() {
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

    protected void setListPreferenceData(ListPreference listPreference, boolean z) {
        listPreference.setEntries(this.videoQualityEntries);
        listPreference.setEntryValues(this.videoQualityentryValues);
        String string = this.sharedPreferences.getString(z ? "pref_preferred_video_quality_wifi" : "pref_preferred_video_quality_mobile", "-2");
        if (listPreference.getValue() == null) {
            listPreference.setValue(string);
        }
        listPreference.setSummary(this.videoQualityEntries[listPreference.findIndexOfValue(string)]);
    }

    protected void setMinimizedListPreferenceData(ListPreference listPreference) {
        listPreference.setEntries(this.minimizedVideoEntries);
        listPreference.setEntryValues(this.minimizedVideoentryValues);
        String string = this.sharedPreferences.getString("pref_minimized_video_preview", "-2");
        if (listPreference.getValue() == null) {
            listPreference.setValue(string);
        }
        listPreference.setSummary(this.minimizedVideoEntries[listPreference.findIndexOfValue(string)]);
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

    private void RestartApplication() {
        Intent launchIntentForPackage = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
        launchIntentForPackage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(launchIntentForPackage);
        getActivity().finish();
    }

    @Override // android.preference.PreferenceFragment, android.app.Fragment
    public void onDestroy() {
        if (this.Registered) {
            this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this.listener);
            this.Registered = false;
        }
        super.onDestroy();
    }
}
