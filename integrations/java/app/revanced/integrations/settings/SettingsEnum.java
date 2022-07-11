package app.revanced.integrations.settings;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum SettingsEnum {

    //Codec Override
    CODEC_OVERRIDE_BOOLEAN("revanced_override_codec_enabled", true),
    MANUFACTURER_OVERRIDE_STRING("revanced_override_codec_manufacturer", null),
    MODEL_OVERRIDE_STRING("revanced_override_codec_model", null),

    //Video Settings
    OLD_STYLE_QUALITY_SETTINGS_BOOLEAN("revanced_use_old_style_quality_settings", true),
    OVERRIDE_RESOLUTION_TO_MAX_BOOLEAN("revanced_override_resolution_max", false),
    PREFERRED_RESOLUTION_WIFI_INTEGER("revanced_pref_video_quality_wifi", -2),
    PREFERRED_RESOLUTION_MOBILE_INTEGER("revanced_pref_video_quality_mobile", -2),
    PREFERRED_VIDEO_SPEED_FLOAT("revanced_pref_video_speed", -2.0f),

    //Whitelist Settings
    ENABLE_WHITELIST_BOOLEAN("revanced_whitelist_ads_enabled", false),

    //Ad settings
    HOME_ADS_SHOWN_BOOLEAN("revanced_home_ads_enabled", false),
    VIDEO_ADS_SHOWN_BOOLEAN("revanced_video_ads_enabled", false),
    ADREMOVER_AD_REMOVAL_BOOLEAN("revanced_adremover_ad_removal", true),
    ADREMOVER_MERCHANDISE_REMOVAL_BOOLEAN("revanced_adremover_merchandise", true),
    ADREMOVER_COMMUNITY_POSTS_REMOVAL_BOOLEAN("revanced_adremover_community_posts_removal", false),
    ADREMOVER_COMPACT_BANNER_REMOVAL_BOOLEAN("revanced_adremover_compact_banner_removal", true),
    ADREMOVER_COMMENTS_REMOVAL_BOOLEAN("revanced_adremover_comments_removal", false),
    ADREMOVER_MOVIE_REMOVAL_BOOLEAN("revanced_adremover_movie", true),
    ADREMOVER_FEED_SURVEY_REMOVAL_BOOLEAN("revanced_adremover_feed_survey", true),
    ADREMOVER_SHORTS_SHELF_BOOLEAN("revanced_adremover_shorts_shelf", true),
    ADREMOVER_COMMUNITY_GUIDELINES_BOOLEAN("revanced_adremover_community_guidelines", true),
    //ToDo: These Settings have to be added to revanced_prefs.xml
    ADREMOVER_EMERGENCY_BOX_REMOVAL_BOOLEAN("revanced_adremover_emergency_box_removal", true),
    ADREMOVER_INFO_PANEL_REMOVAL_BOOLEAN("revanced_adremover_info_panel", true),
    ADREMOVER_MEDICAL_PANEL_REMOVAL_BOOLEAN("revanced_adremover_medical_panel", true),
    ADREMOVER_PAID_CONTECT_REMOVAL_BOOLEAN("revanced_adremover_paid_content", true),
    ADREMOVER_SUGGESTED_FOR_YOU_REMOVAL_BOOLEAN("revanced_adremover_suggested", true),
    ADREMOVER_HIDE_SUGGESTIONS_BOOLEAN("revanced_adremover_hide_suggestions", true),
    ADREMOVER_HIDE_LATEST_POSTS_BOOLEAN("revanced_adremover_hide_latest_posts", true),

    //Layout settings
    REEL_BUTTON_SHOWN_BOOLEAN("revanced_reel_button_enabled", false),
    INFO_CARDS_SHOWN_BOOLEAN("revanced_info_cards_enabled", false),
    BRANDING_SHOWN_BOOLEAN("revanced_branding_watermark_enabled", false),
    CAST_BUTTON_SHOWN_BOOLEAN("revanced_cast_button_enabled", false),
    USE_TABLET_MINIPLAYER_BOOLEAN("revanced_tablet_miniplayer", false),
    CREATE_BUTTON_SHOWN_BOOLEAN("revanced_create_button_enabled", false),
    USE_NEW_ACTIONBAR_BOOLEAN("revanced_new_actionbar", true),
    SHORTS_BUTTON_SHOWN_BOOLEAN("revanced_shorts_button_enabled", false),

    //Misc. Settings
    AUTOREPEAT_BUTTON_SHOWN_BOOLEAN("revanced_pref_auto_repeat_button", false),
    PREFERRED_AUTO_REPEAT_BOOLEAN("revanced_pref_auto_repeat", true),
    USE_HDR_AUTO_BRIGHTNESS_BOOLEAN("revanced_pref_hdr_autobrightness", true),
    TAP_SEEKING_ENABLED_BOOLEAN("revanced_enable_tap_seeking", true),

    //Swipe controls
    ENABLE_SWIPE_BRIGHTNESS_BOOLEAN("revanced_enable_swipe_brightness", true),
    ENABLE_SWIPE_VOLUME_BOOLEAN("revanced_enable_swipe_volume", true),
    ENABLE_PRESS_TO_SWIPE_BOOLEAN("revanced_enable_press_to_swipe", false),
    ENABLE_SWIPE_HAPTIC_FEEDBACK_BOOLEAN("revanced_enable_swipe_haptic_feedback", true),
    SWIPE_OVERLAY_TIMEOUT_LONG("revanced_swipe_overlay_timeout", 500L),
    SWIPE_OVERLAY_TEXT_SIZE_FLOAT("revanced_swipe_overlay_text_size", 22f),
    SWIPE_OVERLAY_BACKGROUND_ALPHA_INTEGER("revanced_swipe_overlay_background_alpha", 127),
    SWIPE_MAGNITUDE_THRESHOLD_FLOAT("revanced_swipe_magnitude_threshold", 30f),

    //Buffer Settings
    MAX_BUFFER_INTEGER("revanced_pref_max_buffer_ms", 120000),
    PLAYBACK_MAX_BUFFER_INTEGER("revanced_pref_buffer_for_playback_ms", 2500),
    MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER("revanced_pref_buffer_for_playback_after_rebuffer_ms", 5000),

    //ReVanced General Settings
    DEBUG_BOOLEAN("revanced_debug_enabled", false),
    USE_DARK_THEME_BOOLEAN("app_theme_dark", false),

    //RYD Settings
    RYD_USER_ID_STRING("ryd_userId", null, SharedPrefHelper.SharedPrefNames.RYD),
    RYD_ENABLED_BOOLEAN("ryd_enabled", true, SharedPrefHelper.SharedPrefNames.RYD),
    RYD_HINT_SHOWN_BOOLEAN("ryd_hint_shown", false, SharedPrefHelper.SharedPrefNames.RYD),

    //SponsorBlock Settings
    SB_ENABLED_BOOLEAN("sb-enabled", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_SHOW_TOAST_WHEN_SKIP_BOOLEAN("show-toast", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_COUNT_SKIPS_BOOLEAN("count-skips", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_UUID_STRING("uuid", null, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_ADJUST_NEW_SEGMENT_STEP_INTEGER("new-segment-step-accuracy", 150, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_MIN_DURATION_FLOAT("sb-min-duration", 0F, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_SPONSOR_BLOCK_HINT_SHOWN_BOOLEAN("sb_hint_shown", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_SEEN_GUIDELINES_BOOLEAN("sb-seen-gl", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_NEW_SEGMENT_ENABLED_BOOLEAN("sb-new-segment-enabled", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_VOTING_ENABLED_BOOLEAN("sb-voting-enabled", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_SKIPPED_SEGMENTS_INTEGER("sb-skipped-segments", 0, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_SKIPPED_SEGMENTS_TIME_LONG("sb-skipped-segments-time", 0L, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_SHOW_TIME_WITHOUT_SEGMENTS_BOOLEAN("sb-length-without-segments", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_IS_VIP_BOOLEAN("sb-is-vip", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_LAST_VIP_CHECK_LONG("sb-last-vip-check", 0L, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_SHOW_BROWSER_BUTTON_BOOLEAN("sb-browser-button", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK),
    SB_API_URL_STRING("sb-api-url", "https://sponsor.ajay.app/api/", SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK);

    private final String path;
    private final Object defaultValue;
    private final SharedPrefHelper.SharedPrefNames sharedPref;

    private Object value = null;
    private static boolean loaded = false;

    SettingsEnum(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = SharedPrefHelper.SharedPrefNames.YOUTUBE;
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
    }

    public static void loadSettings() {
        if (loaded) return;

        Context context = ReVancedUtils.getContext();
        if (context != null) {
            for (SettingsEnum setting : values()) {
                Object value;
                if (setting.name().endsWith("BOOLEAN")) {
                    value = SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath(), (boolean)setting.getDefaultValue());
                } else if (setting.name().endsWith("INTEGER")) {
                    value = SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath(), (int)setting.getDefaultValue());
                } else if (setting.name().endsWith("STRING")) {
                    value = SharedPrefHelper.getString(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath(), (String)setting.getDefaultValue());
                } else if (setting.name().endsWith("LONG")) {
                    value = SharedPrefHelper.getLong(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath(), (long)setting.getDefaultValue());
                } else if (setting.name().endsWith(("FLOAT"))) {
                    value = SharedPrefHelper.getFloat(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, setting.getPath(), (float)setting.getDefaultValue());
                } else {
                    LogHelper.printException(SettingsEnum.class, "Setting does not end with a valid Type. Name is: " + setting.name());
                    continue;
                }


                if (value == null) value = setting.getDefaultValue();
                setting.setValue(value);
            }
            loaded = true;
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static List<SettingsEnum> getAdRemovalSettings() {
        List<SettingsEnum> list = new ArrayList<>();
        for (SettingsEnum var : SettingsEnum.values()) {
            if (var.toString().startsWith("ADREMOVER")) {
                list.add(var);
            }
        }
        return list;
    }

    public void setValue(Object newValue) {
        this.value = newValue;
    }

    public void saveValue(Object newValue) {
        loadSettings();
        Context context = ReVancedUtils.getContext();
        if (context != null) {
            if (name().endsWith("BOOLEAN")) {
                SharedPrefHelper.saveBoolean(context, sharedPref, getPath(), (Boolean) newValue);
            } else if (name().endsWith("INTEGER")) {
                SharedPrefHelper.saveInt(context, sharedPref, getPath(), (int) newValue);
            } else if (name().endsWith("STRING")) {
                SharedPrefHelper.saveString(context, sharedPref, getPath(), (String) newValue);
            } else if (name().endsWith("LONG")) {
                SharedPrefHelper.saveLong(context, sharedPref, getPath(), (Long) newValue);
            } else if (name().endsWith(("FLOAT"))) {
                SharedPrefHelper.saveFloat(context, sharedPref, getPath(), (Float) newValue);
            } else {
                LogHelper.printException(SettingsEnum.class, "Setting does not end with a valid Type. Name is: " + name());
            }
            value = newValue;
        } else {
            LogHelper.printException(SettingsEnum.class, "Context on SaveValue is null!");
        }
    }

    public int getInt() {
        SettingsEnum.loadSettings();
        if (value == null) value = -1;
        return (int) value;
    }

    public String getString() {
        SettingsEnum.loadSettings();
        return (String) value;
    }

    public boolean getBoolean() {
        SettingsEnum.loadSettings();
        return (Boolean) value;
    }

    public Long getLong() {
        SettingsEnum.loadSettings();
        if (value == null) value = -1L;
        return (Long) value;
    }

    public Float getFloat() {
        SettingsEnum.loadSettings();
        if (value == null) value = -1.0f;
        return (Float) value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getPath() {
        return path;
    }

}
