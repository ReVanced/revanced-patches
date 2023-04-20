package app.revanced.integrations.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.BOOLEAN;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.FLOAT;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.INTEGER;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.LONG;
import static app.revanced.integrations.settings.SettingsEnum.ReturnType.STRING;
import static app.revanced.integrations.settings.SharedPrefCategory.RETURN_YOUTUBE_DISLIKE;
import static app.revanced.integrations.settings.SharedPrefCategory.SPONSOR_BLOCK;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.integrations.utils.StringRef;

public enum SettingsEnum {
    //Download Settings
    // TODO: DOWNLOAD_PATH("revanced_download_path", STRING, Environment.getExternalStorageDirectory().getPath() + "/Download"),
    DOWNLOADS_BUTTON_SHOWN("revanced_downloads_enabled", BOOLEAN, TRUE, true),
    DOWNLOADS_PACKAGE_NAME("revanced_downloads_package_name", STRING, "org.schabi.newpipe" /* NewPipe */, parents(DOWNLOADS_BUTTON_SHOWN)),

    // Copy video URL settings
    COPY_VIDEO_URL_BUTTON_SHOWN("revanced_copy_video_url_enabled", BOOLEAN, TRUE, true),
    COPY_VIDEO_URL_TIMESTAMP_BUTTON_SHOWN("revanced_copy_video_url_timestamp_enabled", BOOLEAN, TRUE, true),

    // Video settings
    OLD_STYLE_VIDEO_QUALITY_PLAYER_SETTINGS("revanced_use_old_style_quality_settings", BOOLEAN, TRUE),
    VIDEO_QUALITY_REMEMBER_LAST_SELECTED("revanced_remember_video_quality_last_selected", BOOLEAN, TRUE),
    VIDEO_QUALITY_DEFAULT_WIFI("revanced_default_video_quality_wifi", INTEGER, -2),
    VIDEO_QUALITY_DEFAULT_MOBILE("revanced_default_video_quality_mobile", INTEGER, -2),
    PLAYBACK_SPEED_REMEMBER_LAST_SELECTED("revanced_remember_playback_speed_last_selected", BOOLEAN, TRUE),
    PLAYBACK_SPEED_DEFAULT("revanced_default_playback_speed", FLOAT, 1.0f),

    // TODO: Unused currently
    // Whitelist settings
    //ENABLE_WHITELIST("revanced_whitelist_ads_enabled", BOOLEAN, FALSE),

    // Ad settings
    ADREMOVER_BUTTONED_REMOVAL("revanced_adremover_buttoned", BOOLEAN, TRUE),
    ADREMOVER_CHANNEL_BAR("revanced_hide_channel_bar", BOOLEAN, FALSE),
    ADREMOVER_CHANNEL_MEMBER_SHELF_REMOVAL("revanced_adremover_channel_member_shelf_removal", BOOLEAN, TRUE),
    ADREMOVER_CHAPTER_TEASER_REMOVAL("revanced_adremover_chapter_teaser", BOOLEAN, TRUE),
    ADREMOVER_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_community_guidelines", BOOLEAN, TRUE),
    ADREMOVER_COMMUNITY_POSTS_REMOVAL("revanced_adremover_community_posts_removal", BOOLEAN, FALSE),
    ADREMOVER_COMPACT_BANNER_REMOVAL("revanced_adremover_compact_banner_removal", BOOLEAN, TRUE),
    ADREMOVER_CUSTOM_ENABLED("revanced_adremover_custom_enabled", BOOLEAN, FALSE),
    ADREMOVER_CUSTOM_REMOVAL("revanced_adremover_custom_strings", STRING, "", true, parents(ADREMOVER_CUSTOM_ENABLED)),
    ADREMOVER_EMERGENCY_BOX_REMOVAL("revanced_adremover_emergency_box_removal", BOOLEAN, TRUE),
    ADREMOVER_FEED_SURVEY_REMOVAL("revanced_adremover_feed_survey", BOOLEAN, TRUE),
    ADREMOVER_GENERAL_ADS_REMOVAL("revanced_adremover_ad_removal", BOOLEAN, TRUE),
    ADREMOVER_GRAY_SEPARATOR("revanced_adremover_separator", BOOLEAN, TRUE),
    ADREMOVER_HIDE_CHANNEL_GUIDELINES("revanced_adremover_hide_channel_guidelines", BOOLEAN, TRUE),
    ADREMOVER_HIDE_LATEST_POSTS("revanced_adremover_hide_latest_posts", BOOLEAN, TRUE),
    ADREMOVER_IMAGE_SHELF("revanced_hide_image_shelf", BOOLEAN, TRUE),
    ADREMOVER_INFO_PANEL_REMOVAL("revanced_adremover_info_panel", BOOLEAN, TRUE),
    ADREMOVER_MEDICAL_PANEL_REMOVAL("revanced_adremover_medical_panel", BOOLEAN, TRUE),
    ADREMOVER_MERCHANDISE_REMOVAL("revanced_adremover_merchandise", BOOLEAN, TRUE),
    ADREMOVER_MOVIE_REMOVAL("revanced_adremover_movie", BOOLEAN, TRUE),
    ADREMOVER_PAID_CONTENT_REMOVAL("revanced_adremover_paid_content", BOOLEAN, TRUE),
    ADREMOVER_QUICK_ACTIONS("revanced_hide_quick_actions", BOOLEAN, FALSE),
    ADREMOVER_RELATED_VIDEOS("revanced_hide_related_videos", BOOLEAN, FALSE),
    ADREMOVER_SELF_SPONSOR_REMOVAL("revanced_adremover_self_sponsor", BOOLEAN, TRUE),
    ADREMOVER_SHORTS_REMOVAL("revanced_adremover_shorts", BOOLEAN, TRUE, true),
    ADREMOVER_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_subscribers_community_guidelines_removal", BOOLEAN, TRUE),
    ADREMOVER_VIEW_PRODUCTS("revanced_adremover_view_products", BOOLEAN, TRUE),
    ADREMOVER_WEB_SEARCH_RESULTS("revanced_adremover_web_search_result", BOOLEAN, TRUE),
    VIDEO_ADS_REMOVAL("revanced_video_ads_removal", BOOLEAN, TRUE, true),

    // Action buttons
    HIDE_LIKE_DISLIKE_BUTTON("revanced_hide_like_dislike_button", BOOLEAN, FALSE),
    HIDE_DOWNLOAD_BUTTON("revanced_hide_download_button", BOOLEAN, FALSE),
    HIDE_PLAYLIST_BUTTON("revanced_hide_playlist_button", BOOLEAN, FALSE),
    HIDE_ACTION_BUTTONS("revanced_hide_action_buttons", BOOLEAN, FALSE),

    // Layout settings
    DISABLE_STARTUP_SHORTS_PLAYER("revanced_startup_shorts_player_enabled", BOOLEAN, FALSE),
    HIDE_ALBUM_CARDS("revanced_hide_album_cards", BOOLEAN, FALSE, true),
    HIDE_ARTIST_CARDS("revanced_hide_artist_cards", BOOLEAN, FALSE),
    HIDE_AUTOPLAY_BUTTON("revanced_hide_autoplay_button", BOOLEAN, TRUE, true),
    HIDE_BREAKING_NEWS("revanced_hide_breaking_news", BOOLEAN, TRUE, true),
    HIDE_CAPTIONS_BUTTON("revanced_hide_captions_button", BOOLEAN, FALSE),
    HIDE_CAST_BUTTON("revanced_hide_cast_button", BOOLEAN, TRUE, true),
    HIDE_COMMENTS_SECTION("revanced_hide_comments_section", BOOLEAN, FALSE, true),
    HIDE_CREATE_BUTTON("revanced_hide_create_button", BOOLEAN, TRUE, true),
    HIDE_CROWDFUNDING_BOX("revanced_hide_crowdfunding_box", BOOLEAN, FALSE, true),
    HIDE_EMAIL_ADDRESS("revanced_hide_email_address", BOOLEAN, FALSE),
    HIDE_ENDSCREEN_CARDS("revanced_hide_endscreen_cards", BOOLEAN, TRUE),
    HIDE_FLOATING_MICROPHONE_BUTTON("revanced_hide_floating_microphone_button", BOOLEAN, TRUE, true),
    HIDE_FULLSCREEN_PANELS("revanced_hide_fullscreen_panels", BOOLEAN, TRUE),
    HIDE_INFO_CARDS("revanced_hide_infocards", BOOLEAN, TRUE),
    HIDE_MIX_PLAYLISTS("revanced_hide_mix_playlists", BOOLEAN, FALSE, true),
    HIDE_PLAYER_BUTTONS("revanced_hide_player_buttons", BOOLEAN, FALSE),
    HIDE_PREVIEW_COMMENT("revanced_hide_preview_comment", BOOLEAN, FALSE, true),
    HIDE_SEEKBAR("revanced_hide_seekbar", BOOLEAN, FALSE),
    HIDE_SHORTS_BUTTON("revanced_hide_shorts_button", BOOLEAN, TRUE, true),
    HIDE_SHORTS_COMMENTS_BUTTON("revanced_hide_shorts_comments_button", BOOLEAN, FALSE),
    HIDE_TIMESTAMP("revanced_hide_timestamp", BOOLEAN, FALSE),
    HIDE_VIDEO_WATERMARK("revanced_hide_video_watermark", BOOLEAN, TRUE),
    HIDE_WATCH_IN_VR("revanced_hide_watch_in_vr", BOOLEAN, FALSE, true),
    PLAYER_POPUP_PANELS("revanced_player_popup_panels_enabled", BOOLEAN, FALSE),
    SPOOF_APP_VERSION("revanced_spoof_app_version", BOOLEAN, FALSE, true, "revanced_spoof_app_version_user_dialog_message"),
    USE_TABLET_MINIPLAYER("revanced_tablet_miniplayer", BOOLEAN, FALSE, true),
    WIDE_SEARCHBAR("revanced_wide_searchbar", BOOLEAN, FALSE, true),

    // Misc. Settings
    SIGNATURE_SPOOFING("revanced_spoof_signature_verification", BOOLEAN, TRUE, "revanced_spoof_signature_verification_user_dialog_message"),
    CAPTIONS_ENABLED("revanced_autocaptions_enabled", BOOLEAN, FALSE),
    DISABLE_ZOOM_HAPTICS("revanced_disable_zoom_haptics", BOOLEAN, TRUE),
    ENABLE_EXTERNAL_BROWSER("revanced_enable_external_browser", BOOLEAN, TRUE, true),
    ENABLE_MINIMIZED_PLAYBACK("revanced_enable_minimized_playback", BOOLEAN, TRUE),
    PREFERRED_AUTO_REPEAT("revanced_pref_auto_repeat", BOOLEAN, FALSE),
    TAP_SEEKING_ENABLED("revanced_enable_tap_seeking", BOOLEAN, TRUE),
    USE_HDR_AUTO_BRIGHTNESS("revanced_pref_hdr_autobrightness", BOOLEAN, TRUE),

    // Swipe controls
    ENABLE_SWIPE_BRIGHTNESS("revanced_enable_swipe_brightness", BOOLEAN, TRUE),
    ENABLE_SWIPE_VOLUME("revanced_enable_swipe_volume", BOOLEAN, TRUE),
    ENABLE_PRESS_TO_SWIPE("revanced_enable_press_to_swipe", BOOLEAN, FALSE,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    ENABLE_SWIPE_HAPTIC_FEEDBACK("revanced_enable_swipe_haptic_feedback", BOOLEAN, TRUE,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    SWIPE_MAGNITUDE_THRESHOLD("revanced_swipe_magnitude_threshold", FLOAT, 30f,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    SWIPE_OVERLAY_BACKGROUND_ALPHA("revanced_swipe_overlay_background_alpha", INTEGER, 127,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    SWIPE_OVERLAY_TEXT_SIZE("revanced_swipe_overlay_text_size", FLOAT, 22f,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),
    SWIPE_OVERLAY_TIMEOUT("revanced_swipe_overlay_timeout", LONG, 500L,
            parents(ENABLE_SWIPE_BRIGHTNESS, ENABLE_SWIPE_VOLUME)),

    // Debug settings
    DEBUG("revanced_debug_enabled", BOOLEAN, FALSE),
    DEBUG_STACKTRACE("revanced_debug_stacktrace_enabled", BOOLEAN, FALSE, parents(DEBUG)),
    DEBUG_SHOW_TOAST_ON_ERROR("revanced_debug_toast_on_error_enabled", BOOLEAN, TRUE, "revanced_debug_toast_on_error_user_dialog_message"),

    // ReturnYoutubeDislike settings
    RYD_ENABLED("ryd_enabled", BOOLEAN, TRUE, RETURN_YOUTUBE_DISLIKE),
    RYD_USER_ID("ryd_userId", STRING, "", RETURN_YOUTUBE_DISLIKE),
    RYD_SHOW_DISLIKE_PERCENTAGE("ryd_show_dislike_percentage", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE, parents(RYD_ENABLED)),
    RYD_USE_COMPACT_LAYOUT("ryd_use_compact_layout", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE, parents(RYD_ENABLED)),

    // SponsorBlock settings
    SB_ENABLED("sb-enabled", BOOLEAN, TRUE, SPONSOR_BLOCK),
    SB_VOTING_ENABLED("sb-voting-enabled", BOOLEAN, FALSE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_CREATE_NEW_SEGMENT_ENABLED("sb-new-segment-enabled", BOOLEAN, FALSE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_USE_COMPACT_SKIPBUTTON("sb-use-compact-skip-button", BOOLEAN, FALSE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_SHOW_TOAST_ON_SKIP("show-toast", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_TRACK_SKIP_COUNT("count-skips", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_UUID("uuid", STRING, "", SPONSOR_BLOCK),
    SB_ADJUST_NEW_SEGMENT_STEP("new-segment-step-accuracy", INTEGER, 150, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_MIN_DURATION("sb-min-duration", FLOAT, 0F, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_SEEN_GUIDELINES("sb-seen-gl", BOOLEAN, FALSE, SPONSOR_BLOCK),
    SB_SKIPPED_SEGMENTS_NUMBER_SKIPPED("sb-skipped-segments", INTEGER, 0, SPONSOR_BLOCK),
    SB_SKIPPED_SEGMENTS_TIME_SAVED("sb-skipped-segments-time", LONG, 0L, SPONSOR_BLOCK),
    SB_SHOW_TIME_WITHOUT_SEGMENTS("sb-length-without-segments", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_IS_VIP("sb-is-vip", BOOLEAN, FALSE, SPONSOR_BLOCK),
    SB_LAST_VIP_CHECK("sb-last-vip-check", LONG, 0L, SPONSOR_BLOCK),
    SB_API_URL("sb-api-host-url", STRING, "https://sponsor.ajay.app", SPONSOR_BLOCK);

    private static SettingsEnum[] parents(SettingsEnum ... parents) {
        return parents;
    }

    @NonNull
    public final String path;
    @NonNull
    public final Object defaultValue;
    @NonNull
    public final SharedPrefCategory sharedPref;
    @NonNull
    public final ReturnType returnType;
    /**
     * If the app should be rebooted, if this setting is changed
     */
    public final boolean rebootApp;
    /**
     * Set of boolean parent settings.
     * If any of the parents are enabled, then this setting is available to configure.
     *
     * For example: {@link #DEBUG_STACKTRACE} is non-functional and cannot be configured,
     * unless it's parent {@link #DEBUG} is enabled.
     *
     * Declaration is not needed for items that do not appear in the ReVanced Settings UI.
     */
    @Nullable
    private final SettingsEnum[] parents;

    /**
     * Confirmation message to display, if the user tries to change the setting from the default value.
     * Can only be used for {@link ReturnType#BOOLEAN} setting types.
     */
    @Nullable
    public final StringRef userDialogMessage;

    // Must be volatile, as some settings are read/write from different threads.
    // Of note, the object value is persistently stored using SharedPreferences (which is thread safe).
    @NonNull
    private volatile Object value;

    SettingsEnum(String path, ReturnType returnType, Object defaultValue) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, false, null, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, null,null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 String userDialogMessage) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, false, userDialogMessage, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 SettingsEnum[] parents) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, false, null, parents);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp, String userDialogMessage) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, userDialogMessage, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp, SettingsEnum[] parents) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, null, parents);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue,
                 boolean rebootApp, String userDialogMessage, SettingsEnum[] parents) {
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, userDialogMessage, parents);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName) {
        this(path, returnType, defaultValue, prefName, false, null, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                 boolean rebootApp) {
        this(path, returnType, defaultValue, prefName, rebootApp, null, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                 String userDialogMessage) {
        this(path, returnType, defaultValue, prefName, false, userDialogMessage, null);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                 SettingsEnum[] parents) {
        this(path, returnType, defaultValue, prefName, false, null, parents);
    }
    SettingsEnum(String path, ReturnType returnType, Object defaultValue, SharedPrefCategory prefName,
                 boolean rebootApp, @Nullable String userDialogMessage, @Nullable SettingsEnum[]  parents) {
        this.path = Objects.requireNonNull(path);
        this.returnType = Objects.requireNonNull(returnType);
        this.value = this.defaultValue = Objects.requireNonNull(defaultValue);
        this.sharedPref = Objects.requireNonNull(prefName);
        this.rebootApp = rebootApp;

        if (userDialogMessage == null) {
            this.userDialogMessage = null;
        } else {
            if (returnType != ReturnType.BOOLEAN) {
                throw new IllegalArgumentException("must be Boolean type: " + path);
            }
            this.userDialogMessage = new StringRef(userDialogMessage);
        }

        this.parents = parents;
        if (parents != null) {
            for (SettingsEnum parent : parents) {
                if (parent.returnType != ReturnType.BOOLEAN) {
                    throw new IllegalArgumentException("parent must be Boolean type: " + parent);
                }
            }
        }
    }

    static {
        loadAllSettings();
    }

    @Nullable
    public static SettingsEnum settingFromPath(@NonNull String str) {
        for (SettingsEnum setting : values()) {
            if (setting.path.equals(str)) return setting;
        }
        return null;
    }

    private static void loadAllSettings() {
        for (SettingsEnum setting : values()) {
            setting.load();
        }
    }

    private void load() {
        switch (returnType) {
            case BOOLEAN:
                value = sharedPref.getBoolean(path, (boolean) defaultValue);
                break;
            case INTEGER:
                value = sharedPref.getIntegerString(path, (Integer) defaultValue);
                break;
            case LONG:
                value = sharedPref.getLongString(path, (Long) defaultValue);
                break;
            case FLOAT:
                value = sharedPref.getFloatString(path, (Float) defaultValue);
                break;
            case STRING:
                value = sharedPref.getString(path, (String) defaultValue);
                break;
            default:
                throw new IllegalStateException(name());
        }
    }

    /**
     * Sets, but does _not_ persistently save the value.
     *
     * This intentionally is a static method, to deter accidental usage
     * when {@link #saveValue(Object)} was intended.
     *
     * This method is only to be used by the Settings preference code.
     */
    public static void setValue(@NonNull SettingsEnum setting, @NonNull String newValue) {
        Objects.requireNonNull(newValue);
        switch (setting.returnType) {
            case BOOLEAN:
                setting.value = Boolean.valueOf(newValue);
                break;
            case INTEGER:
                setting.value = Integer.valueOf(newValue);
                break;
            case LONG:
                setting.value = Long.valueOf(newValue);
                break;
            case FLOAT:
                setting.value = Float.valueOf(newValue);
                break;
            case STRING:
                setting.value = newValue;
                break;
            default:
                throw new IllegalStateException(setting.name());
        }
    }
    /**
     * This method is only to be used by the Settings preference code.
     */
    public static void setValue(@NonNull SettingsEnum setting, @NonNull Boolean newValue) {
        Objects.requireNonNull(newValue);
        setting.value = newValue;
    }

    /**
     * Sets the value, and persistently saves it.
     */
    public void saveValue(@NonNull Object newValue) {
        Objects.requireNonNull(newValue);
        switch (returnType) {
            case BOOLEAN:
                sharedPref.saveBoolean(path, (boolean) newValue);
                break;
            case INTEGER:
                sharedPref.saveIntegerString(path, (Integer) newValue);
                break;
            case LONG:
                sharedPref.saveLongString(path, (Long) newValue);
                break;
            case FLOAT:
                sharedPref.saveFloatString(path, (Float) newValue);
                break;
            case STRING:
                sharedPref.saveString(path, (String) newValue);
                break;
            default:
                throw new IllegalStateException(name());
        }
        value = newValue;
    }

    /**
     * @return if this setting can be configured and used.
     *
     * Not to be confused with {@link #getBoolean()}
     */
    public boolean isAvailable() {
        if (parents == null) {
            return true;
        }
        for (SettingsEnum parent : parents) {
            if (parent.getBoolean()) return true;
        }
        return false;
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public int getInt() {
        return (Integer) value;
    }

    public long getLong() {
        return (Long) value;
    }

    public float getFloat() {
        return (Float) value;
    }

    @NonNull
    public String getString() {
        return (String) value;
    }

    /**
     * @return the value of this setting as as generic object type.
     */
    @NonNull
    public Object getObjectValue() {
        return value;
    }

    public enum ReturnType {
        BOOLEAN,
        INTEGER,
        STRING,
        LONG,
        FLOAT,
    }
}