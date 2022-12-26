package app.revanced.integrations.settings;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public enum SettingsEnum {
    //Download Settings
    // TODO: DOWNLOAD_PATH("revanced_download_path", Environment.getExternalStorageDirectory().getPath() + "/Download", ReturnType.STRING),
    DOWNLOADS_BUTTON_SHOWN("revanced_downloads", true, ReturnType.BOOLEAN, true),
    DOWNLOADS_PACKAGE_NAME("revanced_downloads_package_name", "org.schabi.newpipe" /* NewPipe */, ReturnType.STRING),

    // Video settings
    OLD_STYLE_VIDEO_QUALITY_PLAYER_SETTINGS("revanced_use_old_style_quality_settings", true, ReturnType.BOOLEAN),
    PREFERRED_VIDEO_SPEED("revanced_pref_video_speed", -2.0f, ReturnType.FLOAT),
    REMEMBER_VIDEO_QUALITY_LAST_SELECTED("revanced_remember_video_quality_last_selected", true, ReturnType.BOOLEAN),

    // Whitelist settings
    //ToDo: Not used atm, Patch missing
    ENABLE_WHITELIST("revanced_whitelist_ads_enabled", false, ReturnType.BOOLEAN),

    // Ad settings
    ADREMOVER_CUSTOM_ENABLED("revanced_adremover_custom_enabled", false, ReturnType.BOOLEAN),
    ADREMOVER_CUSTOM_REMOVAL("revanced_adremover_custom_strings", "", ReturnType.STRING, true),
    VIDEO_ADS_REMOVAL("revanced_video_ads_removal", true, ReturnType.BOOLEAN, true),
    ADREMOVER_GENERAL_ADS_REMOVAL("revanced_adremover_ad_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_MERCHANDISE_REMOVAL("revanced_adremover_merchandise", true, ReturnType.BOOLEAN),
    ADREMOVER_COMMUNITY_POSTS_REMOVAL("revanced_adremover_community_posts_removal", false, ReturnType.BOOLEAN),
    ADREMOVER_COMPACT_BANNER_REMOVAL("revanced_adremover_compact_banner_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_MOVIE_REMOVAL("revanced_adremover_movie", true, ReturnType.BOOLEAN),
    ADREMOVER_FEED_SURVEY_REMOVAL("revanced_adremover_feed_survey", true, ReturnType.BOOLEAN),
    ADREMOVER_SHORTS_REMOVAL("revanced_adremover_shorts", true, ReturnType.BOOLEAN, true),
    ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_community_guidelines", true, ReturnType.BOOLEAN),
    ADREMOVER_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_subscribers_community_guidelines_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_EMERGENCY_BOX_REMOVAL("revanced_adremover_emergency_box_removal", true, ReturnType.BOOLEAN),
    ADREMOVER_INFO_PANEL_REMOVAL("revanced_adremover_info_panel", true, ReturnType.BOOLEAN),
    ADREMOVER_MEDICAL_PANEL_REMOVAL("revanced_adremover_medical_panel", true, ReturnType.BOOLEAN),
    ADREMOVER_PAID_CONTENT_REMOVAL("revanced_adremover_paid_content", true, ReturnType.BOOLEAN),
    ADREMOVER_SUGGESTIONS_REMOVAL("revanced_adremover_hide_suggestions", true, ReturnType.BOOLEAN),
    ADREMOVER_HIDE_LATEST_POSTS("revanced_adremover_hide_latest_posts", true, ReturnType.BOOLEAN),
    ADREMOVER_HIDE_CHANNEL_GUIDELINES("revanced_adremover_hide_channel_guidelines", true, ReturnType.BOOLEAN),
    ADREMOVER_SELF_SPONSOR_REMOVAL("revanced_adremover_self_sponsor", true, ReturnType.BOOLEAN),
    ADREMOVER_CHAPTER_TEASER_REMOVAL("revanced_adremover_chapter_teaser", true, ReturnType.BOOLEAN),
    ADREMOVER_BUTTONED_REMOVAL("revanced_adremover_buttoned", true, ReturnType.BOOLEAN),
    ADREMOVER_GRAY_SEPARATOR("revanced_adremover_separator", true, ReturnType.BOOLEAN),

    // Action buttons
    HIDE_LIKE_BUTTON("revanced_hide_like_button", false, ReturnType.BOOLEAN, false),
    HIDE_DISLIKE_BUTTON("revanced_hide_dislike_button", false, ReturnType.BOOLEAN, false),
    HIDE_DOWNLOAD_BUTTON("revanced_hide_download_button", false, ReturnType.BOOLEAN, false),
    HIDE_PLAYLIST_BUTTON("revanced_hide_playlist_button", false, ReturnType.BOOLEAN, false),
    HIDE_ACTION_BUTTON("revanced_hide_action_button", false, ReturnType.BOOLEAN, false),
    HIDE_SHARE_BUTTON("revanced_hide_share_button", false, ReturnType.BOOLEAN, false),

    // Layout settings
    DISABLE_STARTUP_SHORTS_PLAYER("revanced_startup_shorts_player_enabled", false, ReturnType.BOOLEAN),
    PLAYER_POPUP_PANELS("revanced_player_popup_panels_enabled", false, ReturnType.BOOLEAN),
    USE_TABLET_MINIPLAYER("revanced_tablet_miniplayer", false, ReturnType.BOOLEAN, true),
    WIDE_SEARCHBAR("revanced_wide_searchbar", false, ReturnType.BOOLEAN, true),
    HIDE_ALBUM_CARDS("revanced_hide_album_cards", false, ReturnType.BOOLEAN, true),
    HIDE_ARTIST_CARD("revanced_hide_artist_card", false, ReturnType.BOOLEAN),
    HIDE_AUTOPLAY_BUTTON("revanced_hide_autoplay_button", true, ReturnType.BOOLEAN, true),
    HIDE_VIDEO_WATERMARK("revanced_hide_video_watermark", true, ReturnType.BOOLEAN),
    HIDE_CAPTIONS_BUTTON("revanced_hide_captions_button", false, ReturnType.BOOLEAN),
    HIDE_CAST_BUTTON("revanced_hide_cast_button", true, ReturnType.BOOLEAN, true),
    HIDE_COMMENTS_SECTION("revanced_hide_comments_section", false, ReturnType.BOOLEAN, true),
    HIDE_CREATE_BUTTON("revanced_hide_create_button", true, ReturnType.BOOLEAN, true),
    HIDE_CROWDFUNDING_BOX("revanced_hide_crowdfunding_box", false, ReturnType.BOOLEAN, true),
    HIDE_EMAIL_ADDRESS("revanced_hide_email_address", false, ReturnType.BOOLEAN),
    HIDE_ENDSCREEN_CARDS("revanced_hide_endscreen_cards", true, ReturnType.BOOLEAN),
    HIDE_FULLSCREEN_PANELS("revanced_hide_fullscreen_panels", true, ReturnType.BOOLEAN), //ToDo: Add to prefs
    HIDE_INFO_CARDS("revanced_hide_infocards", true, ReturnType.BOOLEAN),
    HIDE_MIX_PLAYLISTS("revanced_hide_mix_playlists", false, ReturnType.BOOLEAN, true),
    HIDE_PREVIEW_COMMENT("revanced_hide_preview_comment", false, ReturnType.BOOLEAN, true),
    HIDE_REEL_BUTTON("revanced_hide_reel_button", true, ReturnType.BOOLEAN, true),
    HIDE_SHORTS_BUTTON("revanced_hide_shorts_button", true, ReturnType.BOOLEAN, true),
    HIDE_SHORTS_COMMENTS_BUTTON("revanced_hide_shorts_comments_button", false, ReturnType.BOOLEAN),
    HIDE_TIME_AND_SEEKBAR("revanced_hide_time_and_seekbar", false, ReturnType.BOOLEAN),
    HIDE_WATCH_IN_VR("revanced_hide_watch_in_vr", false, ReturnType.BOOLEAN, true),

    // Misc. Settings
    FIX_PLAYBACK("revanced_fix_playback", false, ReturnType.BOOLEAN, false),
    CAPTIONS_ENABLED("revanced_autocaptions_enabled", false, ReturnType.BOOLEAN, false),
    PREFERRED_AUTO_REPEAT("revanced_pref_auto_repeat", false, ReturnType.BOOLEAN),
    USE_HDR_AUTO_BRIGHTNESS("revanced_pref_hdr_autobrightness", true, ReturnType.BOOLEAN),
    TAP_SEEKING_ENABLED("revanced_enable_tap_seeking", true, ReturnType.BOOLEAN),
    ENABLE_MINIMIZED_PLAYBACK("revanced_enable_minimized_playback", true, ReturnType.BOOLEAN),
    OPEN_LINKS_DIRECTLY("revanced_uri_redirect", true, ReturnType.BOOLEAN, true),
    DISABLE_ZOOM_HAPTICS("revanced_disable_zoom_haptics", true, ReturnType.BOOLEAN, false),

    // Swipe controls
    ENABLE_SWIPE_BRIGHTNESS("revanced_enable_swipe_brightness", true, ReturnType.BOOLEAN),
    ENABLE_SWIPE_VOLUME("revanced_enable_swipe_volume", true, ReturnType.BOOLEAN),
    ENABLE_PRESS_TO_SWIPE("revanced_enable_press_to_swipe", false, ReturnType.BOOLEAN),
    ENABLE_SWIPE_HAPTIC_FEEDBACK("revanced_enable_swipe_haptic_feedback", true, ReturnType.BOOLEAN),
    SWIPE_OVERLAY_TIMEOUT("revanced_swipe_overlay_timeout", 500L, ReturnType.LONG),
    SWIPE_OVERLAY_TEXT_SIZE("revanced_swipe_overlay_text_size", 22f, ReturnType.FLOAT),
    SWIPE_OVERLAY_BACKGROUND_ALPHA("revanced_swipe_overlay_background_alpha", 127, ReturnType.INTEGER),
    SWIPE_MAGNITUDE_THRESHOLD("revanced_swipe_magnitude_threshold", 30f, ReturnType.FLOAT),

    // Buffer settings
    MAX_BUFFER("revanced_pref_max_buffer_ms", 120000, ReturnType.INTEGER),
    PLAYBACK_MAX_BUFFER("revanced_pref_buffer_for_playback_ms", 2500, ReturnType.INTEGER),
    MAX_PLAYBACK_BUFFER_AFTER_REBUFFER("revanced_pref_buffer_for_playback_after_rebuffer_ms", 5000, ReturnType.INTEGER),

    // ReVanced settings
    DEBUG("revanced_debug_enabled", false, ReturnType.BOOLEAN),
    DEBUG_STACKTRACE("revanced_debug_stacktrace_enabled", false, ReturnType.BOOLEAN),

    USE_DARK_THEME("app_theme_dark", false, ReturnType.BOOLEAN),

    // RYD settings
    RYD_USER_ID("ryd_userId", null, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.STRING),
    RYD_ENABLED("ryd_enabled", true, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.BOOLEAN),
    RYD_SHOW_DISLIKE_PERCENTAGE("ryd_show_dislike_percentage", false, SharedPrefHelper.SharedPrefNames.RYD, ReturnType.BOOLEAN),

    // SponsorBlock settings
    SB_ENABLED("sb-enabled", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_SHOW_TOAST_WHEN_SKIP("show-toast", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_COUNT_SKIPS("count-skips", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_UUID("uuid", "", SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.STRING),
    SB_ADJUST_NEW_SEGMENT_STEP("new-segment-step-accuracy", 150, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.INTEGER),
    SB_MIN_DURATION("sb-min-duration", 0F, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.FLOAT),
    SB_SEEN_GUIDELINES("sb-seen-gl", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_NEW_SEGMENT_ENABLED("sb-new-segment-enabled", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_VOTING_ENABLED("sb-voting-enabled", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_SKIPPED_SEGMENTS("sb-skipped-segments", 0, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.INTEGER),
    SB_SKIPPED_SEGMENTS_TIME("sb-skipped-segments-time", 0L, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.LONG),
    SB_SHOW_TIME_WITHOUT_SEGMENTS("sb-length-without-segments", true, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_IS_VIP("sb-is-vip", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_LAST_VIP_CHECK("sb-last-vip-check", 0L, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.LONG),
    SB_SHOW_BROWSER_BUTTON("sb-browser-button", false, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.BOOLEAN),
    SB_API_URL("sb-api-url", "https://sponsor.ajay.app/api/", SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, ReturnType.STRING),


    //
    // old deprecated settings, kept around to migrate user settings on existing installations
    // FIXME: after a few months, eventually delete these settings
    //
    @Deprecated
    DEPRECATED_HIDE_MIX_PLAYLISTS("revanced_mix_playlists_hidden", false, ReturnType.BOOLEAN, true),
    @Deprecated
    DEPRECATED_HIDE_LIKE_BUTTON("revanced_like_button", false, ReturnType.BOOLEAN, false),
    @Deprecated
    DEPRECATED_HIDE_DISLIKE_BUTTON("revanced_dislike_button", false, ReturnType.BOOLEAN, false),
    @Deprecated
    DEPRECATED_HIDE_DOWNLOAD_BUTTON("revanced_download_button", false, ReturnType.BOOLEAN, false),
    @Deprecated
    DEPRECATED_HIDE_PLAYLIST_BUTTON("revanced_playlist_button", false, ReturnType.BOOLEAN, false),
    @Deprecated
    DEPRECATED_HIDE_ACTION_BUTTON("revanced_action_button", false, ReturnType.BOOLEAN, false),
    @Deprecated
    DEPRECATED_HIDE_SHARE_BUTTON("revanced_share_button", false, ReturnType.BOOLEAN, false),
    @Deprecated
    DEPRECATED_FULLSCREEN_PANELS_SHOWN("revanced_fullscreen_panels_enabled", false, ReturnType.BOOLEAN),
    @Deprecated
    DEPRECATED_CREATE_BUTTON_ENABLED("revanced_create_button_enabled", false, ReturnType.BOOLEAN, true),
    @Deprecated
    DEPRECATED_SHORTS_BUTTON_SHOWN("revanced_shorts_button_enabled", false, ReturnType.BOOLEAN, true),
    @Deprecated
    DEPRECATED_REEL_BUTTON_SHOWN("revanced_reel_button_enabled", false, ReturnType.BOOLEAN, true),
    @Deprecated
    DEPRECATED_AUTOPLAY_BUTTON_SHOWN("revanced_autoplay_button_enabled", false, ReturnType.BOOLEAN, true),
    @Deprecated
    DEPRECATED_CAST_BUTTON_SHOWN("revanced_cast_button_enabled", false, ReturnType.BOOLEAN, true),
    @Deprecated
    DEPRECATED_BRANDING_SHOWN("revanced_branding_watermark_enabled", false, ReturnType.BOOLEAN),
    @Deprecated
    DEPRECATED_REMEMBER_VIDEO_QUALITY("revanced_remember_video_quality_selection", false, ReturnType.BOOLEAN);
    //
    // end deprecated settings
    //

    private final String path;
    private final Object defaultValue;
    private final SharedPrefHelper.SharedPrefNames sharedPref;
    private final ReturnType returnType;
    private final boolean rebootApp;

    // must be volatile, as some settings are read/write from different threads
    // of note, the object value is persistently stored using SharedPreferences (which is thread safe)
    private volatile Object value;

    SettingsEnum(String path, Object defaultValue, ReturnType returnType) {
        this(path, defaultValue, SharedPrefHelper.SharedPrefNames.YOUTUBE, returnType, false);
    }

    SettingsEnum(String path, Object defaultValue, ReturnType returnType, boolean rebootApp) {
        this(path, defaultValue, SharedPrefHelper.SharedPrefNames.YOUTUBE, returnType, rebootApp);
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName, ReturnType returnType) {
        this(path, defaultValue, prefName, returnType, false);
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName, ReturnType returnType, boolean rebootApp) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
        this.returnType = returnType;
        this.rebootApp = rebootApp;
    }

    static {
        load();

        //
        // temporary code to migrate user configuration of old settings into current settings
        // FIXME: eventually delete this code
        //

        // old/new settings where old is default off, and new has inverted value and is default on
        SettingsEnum invertedSettingsToMigrate[][] = {
                {DEPRECATED_FULLSCREEN_PANELS_SHOWN, HIDE_FULLSCREEN_PANELS},
                {DEPRECATED_CREATE_BUTTON_ENABLED, HIDE_CREATE_BUTTON},
                {DEPRECATED_SHORTS_BUTTON_SHOWN, HIDE_SHORTS_BUTTON},
                {DEPRECATED_REEL_BUTTON_SHOWN, HIDE_REEL_BUTTON},
                {DEPRECATED_AUTOPLAY_BUTTON_SHOWN, HIDE_AUTOPLAY_BUTTON},
                {DEPRECATED_CAST_BUTTON_SHOWN, HIDE_CAST_BUTTON},
                {DEPRECATED_BRANDING_SHOWN, HIDE_VIDEO_WATERMARK},
                {DEPRECATED_REMEMBER_VIDEO_QUALITY, REMEMBER_VIDEO_QUALITY_LAST_SELECTED},
        };
        for (SettingsEnum oldNewSetting[] : invertedSettingsToMigrate) {
            // by default, old setting was default off
            // migrate to new setting of default on
            SettingsEnum oldSetting = oldNewSetting[0];
            SettingsEnum newSetting = oldNewSetting[1];

            // only need to check if old setting was turned on
            if (oldSetting.getBoolean()) {
                // this code will only run once
                LogHelper.printInfo(() -> "Migrating setting: " + oldSetting + " of 'true' to new setting: "
                        + newSetting + " of 'false'");
                newSetting.saveValue(false); // set opposite of old value
                oldSetting.saveValue(false); // clear old value
            }
        }

        //
        // migrate preference of prior 'default off' settings, into replacement setting with different path name but otherwise is identical
        //
        SettingsEnum renamedSettings[][] = {
                {DEPRECATED_HIDE_MIX_PLAYLISTS, HIDE_MIX_PLAYLISTS},
                {DEPRECATED_HIDE_LIKE_BUTTON, HIDE_LIKE_BUTTON},
                {DEPRECATED_HIDE_DISLIKE_BUTTON, HIDE_DISLIKE_BUTTON},
                {DEPRECATED_HIDE_DOWNLOAD_BUTTON, HIDE_DOWNLOAD_BUTTON},
                {DEPRECATED_HIDE_PLAYLIST_BUTTON, HIDE_PLAYLIST_BUTTON},
                {DEPRECATED_HIDE_ACTION_BUTTON, HIDE_ACTION_BUTTON},
                {DEPRECATED_HIDE_SHARE_BUTTON, HIDE_SHARE_BUTTON},
        };
        for (SettingsEnum oldNewSetting[] : renamedSettings) {
            SettingsEnum oldSetting = oldNewSetting[0];
            SettingsEnum newSetting = oldNewSetting[1];

            if (oldSetting.getBoolean()) {
                LogHelper.printInfo(() -> "Migrating enabled setting from: " + oldSetting
                        + " into replacement setting: " + newSetting);
                newSetting.saveValue(true);
                oldSetting.saveValue(false);
            }
        }
        //
        // end temporary code
        //
    }

    private static void load() {
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            Log.e("revanced: SettingsEnum", "Context returned null! Setings NOT initialized");
        } else {
            try {
                for (SettingsEnum setting : values()) {
                    Object value = setting.getDefaultValue();

                    //LogHelper is not initialized here
                    Log.d("revanced: SettingsEnum", "Loading Setting: " + setting.name());

                    switch (setting.getReturnType()) {
                        case FLOAT:
                            value = SharedPrefHelper.getFloat(context, setting.sharedPref, setting.getPath(), (float) setting.getDefaultValue());
                            break;
                        case LONG:
                            value = SharedPrefHelper.getLong(context, setting.sharedPref, setting.getPath(), (long) setting.getDefaultValue());
                            break;
                        case BOOLEAN:
                            value = SharedPrefHelper.getBoolean(context, setting.sharedPref, setting.getPath(), (boolean) setting.getDefaultValue());
                            break;
                        case INTEGER:
                            value = SharedPrefHelper.getInt(context, setting.sharedPref, setting.getPath(), (int) setting.getDefaultValue());
                            break;
                        case STRING:
                            value = SharedPrefHelper.getString(context, setting.sharedPref, setting.getPath(), (String) setting.getDefaultValue());
                            break;
                        default:
                            LogHelper.printException(() -> ("Setting does not have a valid Type. Name is: " + setting.name()));
                            break;
                    }
                    setting.setValue(value);

                    //LogHelper is not initialized here
                    Log.d("revanced: SettingsEnum", "Loaded Setting: " + setting.name() + " Value: " + value);
                }
            } catch (Throwable th) {
                LogHelper.printException(() -> ("Error during load()!"), th);
            }
        }
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

    /**
     * Sets, but does _not_ persistently save the value.
     *
     * @see #saveValue(Object)
     */
    public void setValue(Object newValue) {
        this.value = newValue;
    }

    /**
     * Sets the value, and persistently saves it
     */
    public void saveValue(Object newValue) {
        Context context = ReVancedUtils.getContext();
        if (context != null) {
            if (returnType == ReturnType.BOOLEAN) {
                SharedPrefHelper.saveBoolean(context, sharedPref, path, (Boolean) newValue);
            } else {
                SharedPrefHelper.saveString(context, sharedPref, path, newValue + "");
            }
            value = newValue;
        } else {
            LogHelper.printException(() -> ("Context on SaveValue is null!"));
        }
    }

    public int getInt() {
        return (int) value;
    }

    public String getString() {
        return (String) value;
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public Long getLong() {
        return (Long) value;
    }

    public Float getFloat() {
        return (Float) value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getPath() {
        return path;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public boolean shouldRebootOnChange() {
        return rebootApp;
    }
}
