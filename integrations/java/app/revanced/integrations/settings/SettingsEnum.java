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
import static app.revanced.integrations.utils.StringRef.str;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.StringRef;

public enum SettingsEnum {
    // External downloader
    EXTERNAL_DOWNLOADER("revanced_external_downloader", BOOLEAN, TRUE),
    EXTERNAL_DOWNLOADER_PACKAGE_NAME("revanced_external_downloader_name", STRING,
            "org.schabi.newpipe" /* NewPipe */, parents(EXTERNAL_DOWNLOADER)),

    // Copy video URL
    COPY_VIDEO_URL("revanced_copy_video_url", BOOLEAN, TRUE),
    COPY_VIDEO_URL_TIMESTAMP("revanced_copy_video_url_timestamp", BOOLEAN, TRUE),

    // Video
    HDR_AUTO_BRIGHTNESS("revanced_hdr_auto_brightness", BOOLEAN, TRUE),
    SHOW_OLD_VIDEO_MENU("revanced_show_old_video_menu", BOOLEAN, TRUE),
    REMEMBER_VIDEO_QUALITY_LAST_SELECTED("revanced_remember_video_quality_last_selected", BOOLEAN, TRUE),
    VIDEO_QUALITY_DEFAULT_WIFI("revanced_video_quality_default_wifi", INTEGER, -2),
    VIDEO_QUALITY_DEFAULT_MOBILE("revanced_video_quality_default_mobile", INTEGER, -2),
    REMEMBER_PLAYBACK_SPEED_LAST_SELECTED("revanced_remember_playback_speed_last_selected", BOOLEAN, TRUE),
    PLAYBACK_SPEED_DEFAULT("revanced_playback_speed_default", FLOAT, 1.0f),
    CUSTOM_PLAYBACK_SPEEDS("revanced_custom_playback_speeds", STRING,
            "0.25\n0.5\n0.75\n0.9\n0.95\n1.0\n1.05\n1.1\n1.25\n1.5\n1.75\n2.0\n3.0\n4.0\n5.0", true),

    // Whitelist
    //WHITELIST("revanced_whitelist_ads", BOOLEAN, FALSE), // TODO: Unused currently

    // Ads
    HIDE_BUTTONED_ADS("revanced_hide_buttoned_ads", BOOLEAN, TRUE),
    HIDE_GENERAL_ADS("revanced_hide_general_ads", BOOLEAN, TRUE),
    HIDE_HIDE_LATEST_POSTS("revanced_hide_latest_posts_ads", BOOLEAN, TRUE),
    HIDE_PAID_CONTENT("revanced_hide_paid_content_ads", BOOLEAN, TRUE),
    HIDE_SELF_SPONSOR("revanced_hide_self_sponsor_ads", BOOLEAN, TRUE),
    HIDE_VIDEO_ADS("revanced_hide_video_ads", BOOLEAN, TRUE, true),
    CUSTOM_FILTER("revanced_custom_filter", BOOLEAN, FALSE),
    CUSTOM_FILTER_STRINGS("revanced_custom_filter_strings", STRING, "", true, parents(CUSTOM_FILTER)),

    // Layout
    HIDE_CHANNEL_BAR("revanced_hide_channel_bar", BOOLEAN, FALSE),
    HIDE_CHANNEL_MEMBER_SHELF("revanced_hide_channel_member_shelf", BOOLEAN, TRUE),
    HIDE_CHAPTER_TEASER("revanced_hide_chapter_teaser", BOOLEAN, TRUE),
    HIDE_COMMUNITY_GUIDELINES("revanced_hide_community_guidelines", BOOLEAN, TRUE),
    HIDE_COMMUNITY_POSTS("revanced_hide_community_posts", BOOLEAN, FALSE),
    HIDE_COMPACT_BANNER("revanced_hide_compact_banner", BOOLEAN, TRUE),
    HIDE_EMERGENCY_BOX("revanced_hide_emergency_box", BOOLEAN, TRUE),
    HIDE_FEED_SURVEY("revanced_hide_feed_survey", BOOLEAN, TRUE),
    HIDE_GRAY_SEPARATOR("revanced_hide_gray_separator", BOOLEAN, TRUE),
    HIDE_HIDE_CHANNEL_GUIDELINES("revanced_hide_channel_guidelines", BOOLEAN, TRUE),
    HIDE_IMAGE_SHELF("revanced_hide_image_shelf", BOOLEAN, TRUE),
    HIDE_HIDE_INFO_PANELS("revanced_hide_info_panels", BOOLEAN, TRUE),
    HIDE_MEDICAL_PANELS("revanced_hide_medical_panels", BOOLEAN, TRUE),
    HIDE_MERCHANDISE_BANNERS("revanced_hide_merchandise_banners", BOOLEAN, TRUE),
    HIDE_MOVIES_SECTION("revanced_hide_movies_section", BOOLEAN, TRUE),
    HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES("revanced_hide_subscribers_community_guidelines", BOOLEAN, TRUE),
    HIDE_PRODUCTS_BANNER("revanced_hide_products_banner", BOOLEAN, TRUE),
    HIDE_WEB_SEARCH_RESULTS("revanced_hide_web_search_results", BOOLEAN, TRUE),
    HIDE_SHORTS("revanced_hide_shorts", BOOLEAN, TRUE, true),
    HIDE_QUICK_ACTIONS("revanced_hide_quick_actions", BOOLEAN, FALSE),
    HIDE_RELATED_VIDEOS("revanced_hide_related_videos", BOOLEAN, FALSE),

    // Action buttons
    HIDE_LIKE_DISLIKE_BUTTON("revanced_hide_like_dislike_button", BOOLEAN, FALSE),
    HIDE_DOWNLOAD_BUTTON("revanced_hide_download_button", BOOLEAN, FALSE),
    HIDE_PLAYLIST_BUTTON("revanced_hide_playlist_button", BOOLEAN, FALSE),
    HIDE_CLIP_BUTTON("revanced_hide_clip_button", BOOLEAN, FALSE, "revanced_hide_clip_button_user_dialog_message"),
    HIDE_ACTION_BUTTONS("revanced_hide_action_buttons", BOOLEAN, FALSE),

    // Layout
    DISABLE_RESUMING_SHORTS_PLAYER("revanced_disable_resuming_shorts_player", BOOLEAN, FALSE),
    HIDE_ALBUM_CARDS("revanced_hide_album_cards", BOOLEAN, FALSE, true),
    HIDE_ARTIST_CARDS("revanced_hide_artist_cards", BOOLEAN, FALSE),
    HIDE_AUDIO_TRACK_BUTTON("revanced_hide_audio_track_button", BOOLEAN, FALSE),
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
    HIDE_GET_PREMIUM("revanced_hide_get_premium", BOOLEAN, TRUE),
    HIDE_INFO_CARDS("revanced_hide_info_cards", BOOLEAN, TRUE),
    HIDE_LOAD_MORE_BUTTON("revanced_hide_load_more_button", BOOLEAN, TRUE, true),
    HIDE_PLAYER_BUTTONS("revanced_hide_player_buttons", BOOLEAN, FALSE),
    HIDE_PLAYER_OVERLAY("revanced_hide_player_overlay", BOOLEAN, FALSE, true),
    HIDE_PREVIEW_COMMENT("revanced_hide_preview_comment", BOOLEAN, FALSE, true),
    HIDE_SEEKBAR("revanced_hide_seekbar", BOOLEAN, FALSE, true),
    HIDE_HOME_BUTTON("revanced_hide_home_button", BOOLEAN, FALSE, true),
    HIDE_SHORTS_BUTTON("revanced_hide_shorts_button", BOOLEAN, TRUE, true),
    HIDE_SUBSCRIPTIONS_BUTTON("revanced_hide_subscriptions_button", BOOLEAN, FALSE, true),
    HIDE_SHORTS_COMMENTS_BUTTON("revanced_hide_shorts_comments_button", BOOLEAN, FALSE),
    HIDE_TIMESTAMP("revanced_hide_timestamp", BOOLEAN, FALSE),
    HIDE_VIDEO_WATERMARK("revanced_hide_video_watermark", BOOLEAN, TRUE),
    HIDE_WATCH_IN_VR("revanced_hide_watch_in_vr", BOOLEAN, FALSE, true),
    PLAYER_POPUP_PANELS("revanced_hide_player_popup_panels", BOOLEAN, FALSE),
    SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON("revanced_switch_create_with_notifications_button", BOOLEAN, TRUE, true),
    SPOOF_APP_VERSION("revanced_spoof_app_version", BOOLEAN, FALSE, true, "revanced_spoof_app_version_user_dialog_message"),
    SPOOF_APP_VERSION_TARGET("revanced_spoof_app_version_target", STRING, "17.30.35", true, parents(SPOOF_APP_VERSION)),
    USE_TABLET_MINIPLAYER("revanced_tablet_miniplayer", BOOLEAN, FALSE, true),
    WIDE_SEARCHBAR("revanced_wide_searchbar", BOOLEAN, FALSE, true),
    SEEKBAR_COLOR("revanced_seekbar_color", STRING, "#FF0000", true),
    HIDE_FILTER_BAR_FEED_IN_FEED("revanced_hide_filter_bar_feed_in_feed", BOOLEAN, FALSE, true),
    HIDE_FILTER_BAR_FEED_IN_SEARCH("revanced_hide_filter_bar_feed_in_search", BOOLEAN, FALSE, true),
    HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS("revanced_hide_filter_bar_feed_in_related_videos", BOOLEAN, FALSE, true),

    // Misc
    AUTO_CAPTIONS("revanced_auto_captions", BOOLEAN, FALSE),
    DISABLE_ZOOM_HAPTICS("revanced_disable_zoom_haptics", BOOLEAN, TRUE),
    EXTERNAL_BROWSER("revanced_external_browser", BOOLEAN, TRUE, true),
    AUTO_REPEAT("revanced_auto_repeat", BOOLEAN, FALSE),
    TAP_SEEKING("revanced_tap_seeking", BOOLEAN, TRUE),
    SPOOF_SIGNATURE_VERIFICATION("revanced_spoof_signature_verification", BOOLEAN, TRUE, "revanced_spoof_signature_verification_user_dialog_message"),

    // Swipe controls
    SWIPE_BRIGHTNESS("revanced_swipe_brightness", BOOLEAN, TRUE),
    SWIPE_VOLUME("revanced_swipe_volume", BOOLEAN, TRUE),
    SWIPE_PRESS_TO_ENGAGE("revanced_swipe_press_to_engage", BOOLEAN, FALSE, true,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_HAPTIC_FEEDBACK("revanced_swipe_haptic_feedback", BOOLEAN, TRUE,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_MAGNITUDE_THRESHOLD("revanced_swipe_threshold", INTEGER, 30,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_OVERLAY_BACKGROUND_ALPHA("revanced_swipe_overlay_background_alpha", INTEGER, 127,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_OVERLAY_TEXT_SIZE("revanced_swipe_text_overlay_size", INTEGER, 22,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_OVERLAY_TIMEOUT("revanced_swipe_overlay_timeout", LONG, 500L,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),

    // Debugging
    DEBUG("revanced_debug", BOOLEAN, FALSE),
    DEBUG_STACKTRACE("revanced_debug_stacktrace", BOOLEAN, FALSE, parents(DEBUG)),
    DEBUG_TOAST_ON_ERROR("revanced_debug_toast_on_error", BOOLEAN, TRUE, "revanced_debug_toast_on_error_user_dialog_message"),

    // ReturnYoutubeDislike
    RYD_ENABLED("ryd_enabled", BOOLEAN, TRUE, RETURN_YOUTUBE_DISLIKE),
    RYD_USER_ID("ryd_user_id", STRING, "", RETURN_YOUTUBE_DISLIKE),
    RYD_SHORTS("ryd_shorts", BOOLEAN, TRUE, RETURN_YOUTUBE_DISLIKE, parents(RYD_ENABLED)),
    RYD_DISLIKE_PERCENTAGE("ryd_dislike_percentage", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE, parents(RYD_ENABLED)),
    RYD_COMPACT_LAYOUT("ryd_compact_layout", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE, parents(RYD_ENABLED)),
    RYD_TOAST_ON_CONNECTION_ERROR("ryd_toast_on_connection_error", BOOLEAN, TRUE, RETURN_YOUTUBE_DISLIKE, parents(RYD_ENABLED)),

    // SponsorBlock
    SB_ENABLED("sb_enabled", BOOLEAN, TRUE, SPONSOR_BLOCK),
    SB_PRIVATE_USER_ID("sb_private_user_id_Do_Not_Share", STRING, "", SPONSOR_BLOCK), /** Do not use directly, instead use {@link SponsorBlockSettings} */
    DEPRECATED_SB_UUID_OLD_MIGRATION_SETTING("uuid", STRING, "", SPONSOR_BLOCK), // Delete sometime in 2024
    SB_CREATE_NEW_SEGMENT_STEP("sb_create_new_segment_step", INTEGER, 150, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_VOTING_BUTTON("sb_voting_button", BOOLEAN, FALSE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_CREATE_NEW_SEGMENT("sb_create_new_segment", BOOLEAN, FALSE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_COMPACT_SKIP_BUTTON("sb_compact_skip_button", BOOLEAN, FALSE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_AUTO_HIDE_SKIP_BUTTON("sb_auto_hide_skip_button", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_TOAST_ON_SKIP("sb_toast_on_skip", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_TOAST_ON_CONNECTION_ERROR("sb_toast_on_connection_error", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_TRACK_SKIP_COUNT("sb_track_skip_count", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_SEGMENT_MIN_DURATION("sb_min_segment_duration", FLOAT, 0F, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_VIDEO_LENGTH_WITHOUT_SEGMENTS("sb_video_length_without_segments", BOOLEAN, TRUE, SPONSOR_BLOCK, parents(SB_ENABLED)),
    SB_API_URL("sb_api_url", STRING, "https://sponsor.ajay.app", SPONSOR_BLOCK),
    SB_USER_IS_VIP("sb_user_is_vip", BOOLEAN, FALSE, SPONSOR_BLOCK),
    // SB settings not exported
    SB_LAST_VIP_CHECK("sb_last_vip_check", LONG, 0L, SPONSOR_BLOCK),
    SB_HIDE_EXPORT_WARNING("sb_hide_export_warning", BOOLEAN, FALSE, SPONSOR_BLOCK),
    SB_SEEN_GUIDELINES("sb_seen_guidelines", BOOLEAN, FALSE, SPONSOR_BLOCK),
    SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS("sb_local_time_saved_number_segments", INTEGER, 0, SPONSOR_BLOCK),
    SB_LOCAL_TIME_SAVED_MILLISECONDS("sb_local_time_saved_milliseconds", LONG, 0L, SPONSOR_BLOCK),

    //
    // TODO: eventually, delete these
    //
    @Deprecated
    DEPRECATED_ADREMOVER_BUTTONED_REMOVAL("revanced_adremover_buttoned", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_ADREMOVER_GENERAL_ADS_REMOVAL("revanced_adremover_ad_removal", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_ADREMOVER_PAID_CONTENT("revanced_adremover_paid_content", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_ADREMOVER_HIDE_LATEST_POSTS("revanced_adremover_hide_latest_posts", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_ADREMOVER_SELF_SPONSOR("revanced_adremover_self_sponsor", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_ADREMOVER_CUSTOM_ENABLED("revanced_adremover_custom_enabled", BOOLEAN, FALSE),
    @Deprecated
    DEPRECATED_ADREMOVER_CUSTOM_REMOVAL("revanced_adremover_custom_strings", STRING, "", true),
    @Deprecated
    DEPRECATED_REMOVE_VIDEO_ADS("revanced_video_ads_removal", BOOLEAN, TRUE, true),

    @Deprecated
    DEPRECATED_HIDE_CHANNEL_MEMBER_SHELF("revanced_adremover_channel_member_shelf_removal", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_CHAPTER_TEASER("revanced_adremover_chapter_teaser", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_COMMUNITY_GUIDELINES("revanced_adremover_community_guidelines", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_COMMUNITY_POSTS("revanced_adremover_community_posts_removal", BOOLEAN, FALSE),
    @Deprecated
    DEPRECATED_HIDE_COMPACT_BANNER("revanced_adremover_compact_banner_removal", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_EMERGENCY_BOX("revanced_adremover_emergency_box_removal", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_FEED_SURVEY_REMOVAL("revanced_adremover_feed_survey", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_GRAY_SEPARATOR("revanced_adremover_separator", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_HIDE_CHANNEL_GUIDELINES("revanced_adremover_hide_channel_guidelines", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_INFO_PANEL_REMOVAL("revanced_adremover_info_panel", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_MEDICAL_PANEL_REMOVAL("revanced_adremover_medical_panel", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_MERCHANDISE_REMOVAL("revanced_adremover_merchandise", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_MOVIE_REMOVAL("revanced_adremover_movie", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL("revanced_adremover_subscribers_community_guidelines_removal", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_VIEW_PRODUCTS("revanced_adremover_view_products", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_WEB_SEARCH_RESULTS("revanced_adremover_web_search_result", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HIDE_SHORTS("revanced_adremover_shorts", BOOLEAN, TRUE, true),
    @Deprecated
    DEPRECATED_HIDE_INFO_CARDS("revanced_hide_infocards", BOOLEAN, TRUE),

    @Deprecated
    DEPRECATED_DISABLE_RESUMING_SHORTS_PLAYER("revanced_disable_startup_shorts_player", BOOLEAN, FALSE),

    @Deprecated
    DEPRECATED_ETERNAL_DOWNLOADER("revanced_downloads_enabled", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_EXTERNAL_DOWNLOADER_PACKAGE_NAME("revanced_downloads_package_name", STRING, "org.schabi.newpipe"),

    @Deprecated
    DEPRECATED_SHOW_OLD_VIDEO_MENU("revanced_use_old_style_quality_settings", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_VIDEO_QUALITY_DEFAULT_WIFI("revanced_default_video_quality_wifi", INTEGER, -2),
    @Deprecated
    DEPRECATED_VIDEO_QUALITY_DEFAULT_MOBILE("revanced_default_video_quality_mobile", INTEGER, -2),
    @Deprecated
    DEPRECATED_PLAYBACK_SPEED_DEFAULT("revanced_default_playback_speed", FLOAT, 1.0f),

    @Deprecated
    DEPRECATED_COPY_VIDEO_URL("revanced_copy_video_url_enabled", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_COPY_VIDEO_URL_TIMESTAMP("revanced_copy_video_url_timestamp_enabled", BOOLEAN, TRUE),

    @Deprecated
    DEPRECATED_AUTO_CAPTIONS("revanced_autocaptions_enabled", BOOLEAN, FALSE),
    @Deprecated
    DEPRECATED_PLAYER_POPUP_PANELS("revanced_player_popup_panels_enabled", BOOLEAN, FALSE),
    @Deprecated
    DEPRECATED_SWIPE_BRIGHTNESS("revanced_enable_swipe_brightness", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_SWIPE_VOLUME("revanced_enable_swipe_volume", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_PRESS_TO_SWIPE("revanced_enable_press_to_swipe", BOOLEAN, FALSE),
    @Deprecated
    DEPRECATED_SWIPE_HAPTIC_FEEDBACK("revanced_enable_swipe_haptic_feedback", BOOLEAN, TRUE),

    @Deprecated
    DEPRECATED_DEBUG("revanced_debug_enabled", BOOLEAN, FALSE),
    @Deprecated
    DEPRECATED_DEBUG_STACKTRACE("revanced_debug_stacktrace_enabled", BOOLEAN, FALSE),
    @Deprecated
    DEPRECATED_DEBUG_TOAST_ON_ERROR("revanced_debug_toast_on_error_enabled", BOOLEAN, TRUE),

    @Deprecated
    DEPRECATED_EXTERNAL_BROWSER("revanced_enable_external_browser", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_AUTO_REPEAT("revanced_pref_auto_repeat", BOOLEAN, FALSE),
    @Deprecated
    DEPRECATED_TAP_SEEKING("revanced_enable_tap_seeking", BOOLEAN, TRUE),
    @Deprecated
    DEPRECATED_HDR_AUTO_BRIGHTNESS("revanced_pref_hdr_autobrightness", BOOLEAN, TRUE),

    @Deprecated
    DEPRECATED_RYD_USER_ID("ryd_userId", STRING, "", RETURN_YOUTUBE_DISLIKE),
    @Deprecated
    DEPRECATED_RYD_DISLIKE_PERCENTAGE("ryd_show_dislike_percentage", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE),
    @Deprecated
    DEPRECATED_RYD_COMPACT_LAYOUT("ryd_use_compact_layout", BOOLEAN, FALSE, RETURN_YOUTUBE_DISLIKE),

    @Deprecated
    DEPRECATED_SB_ENABLED("sb-enabled", BOOLEAN, TRUE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_VOTING_BUTTON("sb-voting-enabled", BOOLEAN, FALSE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_CREATE_NEW_SEGMENT("sb-new-segment-enabled", BOOLEAN, FALSE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_COMPACT_SKIP_BUTTON("sb-use-compact-skip-button", BOOLEAN, FALSE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_MIN_DURATION("sb-min-duration", FLOAT, 0F, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_VIDEO_LENGTH_WITHOUT_SEGMENTS("sb-length-without-segments", BOOLEAN, TRUE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_API_URL("sb-api-host-url", STRING, "https://sponsor.ajay.app", SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_TOAST_ON_SKIP("show-toast", BOOLEAN, TRUE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_AUTO_HIDE_SKIP_BUTTON("sb-auto-hide-skip-segment-button", BOOLEAN, TRUE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_TRACK_SKIP_COUNT("count-skips", BOOLEAN, TRUE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_ADJUST_NEW_SEGMENT_STEP("new-segment-step-accuracy", INTEGER, 150, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_LAST_VIP_CHECK("sb-last-vip-check", LONG, 0L, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_IS_VIP("sb-is-vip", BOOLEAN, FALSE, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS("sb-skipped-segments", INTEGER, 0, SPONSOR_BLOCK),
    @Deprecated
    DEPRECATED_SB_LOCAL_TIME_SAVED_MILLISECONDS("sb-skipped-segments-time", LONG, 0L, SPONSOR_BLOCK);
    //
    // TODO END
    //

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

    private static final Map<String, SettingsEnum> pathToSetting = new HashMap<>(2* values().length);

    static {
        loadAllSettings();

        for (SettingsEnum setting : values()) {
            pathToSetting.put(setting.path, setting);
        }
    }

    @Nullable
    public static SettingsEnum settingFromPath(@NonNull String str) {
        return pathToSetting.get(str);
    }

    private static void loadAllSettings() {
        for (SettingsEnum setting : values()) {
            setting.load();
        }

        //
        // TODO: eventually delete this
        // renamed settings with new path names, but otherwise the new and old settings are identical
        //
        SettingsEnum[][] renamedSettings = {
                // TODO: do _not_ delete this SB private user id migration property until sometime in 2024.
                // This is the only setting that cannot be reconfigured if lost,
                // and more time should be given for users who rarely upgrade.
                {DEPRECATED_SB_UUID_OLD_MIGRATION_SETTING, SB_PRIVATE_USER_ID},

                // TODO: delete the rest of these migration settings.  When to delete? Anytime.
                {DEPRECATED_ADREMOVER_BUTTONED_REMOVAL, HIDE_BUTTONED_ADS},
                {DEPRECATED_ADREMOVER_GENERAL_ADS_REMOVAL, HIDE_GENERAL_ADS},
                {DEPRECATED_ADREMOVER_HIDE_LATEST_POSTS, HIDE_HIDE_LATEST_POSTS},
                {DEPRECATED_ADREMOVER_PAID_CONTENT, HIDE_PAID_CONTENT},
                {DEPRECATED_ADREMOVER_SELF_SPONSOR, HIDE_SELF_SPONSOR},
                {DEPRECATED_REMOVE_VIDEO_ADS, HIDE_VIDEO_ADS},
                {DEPRECATED_ADREMOVER_CUSTOM_ENABLED, CUSTOM_FILTER},
                {DEPRECATED_ADREMOVER_CUSTOM_REMOVAL, CUSTOM_FILTER_STRINGS},

                {DEPRECATED_HIDE_CHANNEL_MEMBER_SHELF, HIDE_CHANNEL_MEMBER_SHELF},
                {DEPRECATED_HIDE_CHAPTER_TEASER, HIDE_CHAPTER_TEASER},
                {DEPRECATED_HIDE_COMMUNITY_GUIDELINES, HIDE_COMMUNITY_GUIDELINES},
                {DEPRECATED_HIDE_COMMUNITY_POSTS, HIDE_COMMUNITY_POSTS},
                {DEPRECATED_HIDE_COMPACT_BANNER, HIDE_COMPACT_BANNER},
                {DEPRECATED_HIDE_EMERGENCY_BOX, HIDE_EMERGENCY_BOX},
                {DEPRECATED_HIDE_FEED_SURVEY_REMOVAL, HIDE_FEED_SURVEY},
                {DEPRECATED_HIDE_GRAY_SEPARATOR, HIDE_GRAY_SEPARATOR},
                {DEPRECATED_HIDE_HIDE_CHANNEL_GUIDELINES, HIDE_HIDE_CHANNEL_GUIDELINES},
                {DEPRECATED_HIDE_INFO_PANEL_REMOVAL, HIDE_HIDE_INFO_PANELS},
                {DEPRECATED_HIDE_MEDICAL_PANEL_REMOVAL, HIDE_MEDICAL_PANELS},
                {DEPRECATED_HIDE_MERCHANDISE_REMOVAL, HIDE_MERCHANDISE_BANNERS},
                {DEPRECATED_HIDE_MOVIE_REMOVAL, HIDE_MOVIES_SECTION},
                {DEPRECATED_HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES_REMOVAL, HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES},
                {DEPRECATED_HIDE_VIEW_PRODUCTS, HIDE_PRODUCTS_BANNER},
                {DEPRECATED_HIDE_WEB_SEARCH_RESULTS, HIDE_WEB_SEARCH_RESULTS},
                {DEPRECATED_HIDE_SHORTS, HIDE_SHORTS},
                {DEPRECATED_DISABLE_RESUMING_SHORTS_PLAYER, DISABLE_RESUMING_SHORTS_PLAYER},
                {DEPRECATED_HIDE_INFO_CARDS, HIDE_INFO_CARDS},

                {DEPRECATED_ETERNAL_DOWNLOADER, EXTERNAL_DOWNLOADER},
                {DEPRECATED_EXTERNAL_DOWNLOADER_PACKAGE_NAME, EXTERNAL_DOWNLOADER_PACKAGE_NAME},
                {DEPRECATED_COPY_VIDEO_URL, COPY_VIDEO_URL},
                {DEPRECATED_COPY_VIDEO_URL_TIMESTAMP, COPY_VIDEO_URL_TIMESTAMP},

                {DEPRECATED_SHOW_OLD_VIDEO_MENU, SHOW_OLD_VIDEO_MENU},
                {DEPRECATED_VIDEO_QUALITY_DEFAULT_WIFI, VIDEO_QUALITY_DEFAULT_WIFI},
                {DEPRECATED_VIDEO_QUALITY_DEFAULT_MOBILE, VIDEO_QUALITY_DEFAULT_MOBILE},
                {DEPRECATED_PLAYBACK_SPEED_DEFAULT, PLAYBACK_SPEED_DEFAULT},

                {DEPRECATED_AUTO_CAPTIONS, AUTO_CAPTIONS},
                {DEPRECATED_PLAYER_POPUP_PANELS, PLAYER_POPUP_PANELS},
                {DEPRECATED_SWIPE_BRIGHTNESS, SWIPE_BRIGHTNESS},
                {DEPRECATED_SWIPE_VOLUME, SWIPE_VOLUME},
                {DEPRECATED_PRESS_TO_SWIPE, SWIPE_PRESS_TO_ENGAGE},
                {DEPRECATED_SWIPE_HAPTIC_FEEDBACK, SWIPE_HAPTIC_FEEDBACK},

                {DEPRECATED_DEBUG, DEBUG},
                {DEPRECATED_DEBUG_STACKTRACE, DEBUG_STACKTRACE},
                {DEPRECATED_DEBUG_TOAST_ON_ERROR, DEBUG_TOAST_ON_ERROR},

                {DEPRECATED_EXTERNAL_BROWSER, EXTERNAL_BROWSER},
                {DEPRECATED_AUTO_REPEAT, AUTO_REPEAT},
                {DEPRECATED_TAP_SEEKING, TAP_SEEKING},
                {DEPRECATED_HDR_AUTO_BRIGHTNESS, HDR_AUTO_BRIGHTNESS},

                {DEPRECATED_RYD_USER_ID, RYD_USER_ID},
                {DEPRECATED_RYD_DISLIKE_PERCENTAGE, RYD_DISLIKE_PERCENTAGE},
                {DEPRECATED_RYD_COMPACT_LAYOUT, RYD_COMPACT_LAYOUT},

                {DEPRECATED_SB_ENABLED, SB_ENABLED},
                {DEPRECATED_SB_VOTING_BUTTON, SB_VOTING_BUTTON},
                {DEPRECATED_SB_CREATE_NEW_SEGMENT, SB_CREATE_NEW_SEGMENT},
                {DEPRECATED_SB_COMPACT_SKIP_BUTTON, SB_COMPACT_SKIP_BUTTON},
                {DEPRECATED_SB_MIN_DURATION, SB_SEGMENT_MIN_DURATION},
                {DEPRECATED_SB_VIDEO_LENGTH_WITHOUT_SEGMENTS, SB_VIDEO_LENGTH_WITHOUT_SEGMENTS},
                {DEPRECATED_SB_API_URL, SB_API_URL},
                {DEPRECATED_SB_TOAST_ON_SKIP, SB_TOAST_ON_SKIP},
                {DEPRECATED_SB_AUTO_HIDE_SKIP_BUTTON, SB_AUTO_HIDE_SKIP_BUTTON},
                {DEPRECATED_SB_TRACK_SKIP_COUNT, SB_TRACK_SKIP_COUNT},
                {DEPRECATED_SB_ADJUST_NEW_SEGMENT_STEP, SB_CREATE_NEW_SEGMENT_STEP},
                {DEPRECATED_SB_LAST_VIP_CHECK, SB_LAST_VIP_CHECK},
                {DEPRECATED_SB_IS_VIP, SB_USER_IS_VIP},
                {DEPRECATED_SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS, SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS},
                {DEPRECATED_SB_LOCAL_TIME_SAVED_MILLISECONDS, SB_LOCAL_TIME_SAVED_MILLISECONDS},
        };
        for (SettingsEnum[] oldNewSetting : renamedSettings) {
            SettingsEnum oldSetting = oldNewSetting[0];
            SettingsEnum newSetting = oldNewSetting[1];

            if (!oldSetting.isSetToDefault()) {
                LogHelper.printInfo(() -> "Migrating old setting of '" + oldSetting.value
                        + "' from: " + oldSetting + " into replacement setting: " + newSetting);
                newSetting.saveValue(oldSetting.value);
                oldSetting.saveValue(oldSetting.defaultValue); // reset old value
            }
        }
        //
        // TODO end
        //
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
        setting.returnType.validate(newValue);
        setting.value = newValue;
    }

    /**
     * Sets the value, and persistently saves it.
     */
    public void saveValue(@NonNull Object newValue) {
        returnType.validate(newValue);
        value = newValue; // Must set before saving to preferences (otherwise importing fails to update UI correctly).
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

    /**
     * @return if the currently set value is the same as {@link #defaultValue}
     */
    public boolean isSetToDefault() {
        return value.equals(defaultValue);
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

    /**
     * This could be yet another field,
     * for now use a simple switch statement since this method is not used outside this class.
     */
    private boolean includeWithImportExport() {
        switch (this) {
            case RYD_USER_ID: // Not useful to export, no reason to include it.
            case SB_LAST_VIP_CHECK:
            case SB_HIDE_EXPORT_WARNING:
            case SB_SEEN_GUIDELINES:
            case SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS:
            case SB_LOCAL_TIME_SAVED_MILLISECONDS:
                return false;
        }
        return true;
    }

    // Begin import / export

    /**
     * If a setting path has this prefix, then remove it before importing/exporting.
     */
    private static final String OPTIONAL_REVANCED_SETTINGS_PREFIX = "revanced_";

    /**
     * The path, minus any 'revanced' prefix to keep json concise.
     */
    private String getImportExportKey() {
        if (path.startsWith(OPTIONAL_REVANCED_SETTINGS_PREFIX)) {
            return path.substring(OPTIONAL_REVANCED_SETTINGS_PREFIX.length());
        }
        return path;
    }

    private static SettingsEnum[] valuesSortedForExport() {
        SettingsEnum[] sorted = values();
        Arrays.sort(sorted, (SettingsEnum o1, SettingsEnum o2) -> {
            // Organize SponsorBlock settings last.
            final boolean o1IsSb = o1.sharedPref == SPONSOR_BLOCK;
            final boolean o2IsSb = o2.sharedPref == SPONSOR_BLOCK;
            if (o1IsSb != o2IsSb) {
                return o1IsSb ? 1 : -1;
            }
            return o1.path.compareTo(o2.path);
        });
        return sorted;
    }

    @NonNull
    public static String exportJSON(@Nullable Context alertDialogContext) {
        try {
            JSONObject json = new JSONObject();
            for (SettingsEnum setting : valuesSortedForExport()) {
                String importExportKey = setting.getImportExportKey();
                if (json.has(importExportKey)) {
                    throw new IllegalArgumentException("duplicate key found: " + importExportKey);
                }
                final boolean exportDefaultValues = false; // Enable to see what all settings looks like in the UI.
                if (setting.includeWithImportExport() && (!setting.isSetToDefault() | exportDefaultValues)) {
                    json.put(importExportKey, setting.getObjectValue());
                }
            }
            SponsorBlockSettings.exportCategoriesToFlatJson(alertDialogContext, json);

            if (json.length() == 0) {
                return "";
            }
            String export = json.toString(0);
            // Remove the outer JSON braces to make the output more compact,
            // and leave less chance of the user forgetting to copy it
            return export.substring(2, export.length() - 2);
        } catch (JSONException e) {
            LogHelper.printException(() -> "Export failure", e); // should never happen
            return "";
        }
    }

    /**
     * @return if any settings that require a reboot were changed.
     */
    public static boolean importJSON(@NonNull String settingsJsonString) {
        try {
            if (!settingsJsonString.matches("[\\s\\S]*\\{")) {
                settingsJsonString = '{' + settingsJsonString + '}'; // Restore outer JSON braces
            }
            JSONObject json = new JSONObject(settingsJsonString);

            boolean rebootSettingChanged = false;
            int numberOfSettingsImported = 0;
            for (SettingsEnum setting : values()) {
                String key = setting.getImportExportKey();
                if (json.has(key)) {
                    Object value;
                    switch (setting.returnType) {
                        case BOOLEAN:
                             value = json.getBoolean(key);
                             break;
                        case INTEGER:
                            value = json.getInt(key);
                            break;
                        case LONG:
                            value = json.getLong(key);
                            break;
                        case FLOAT:
                            value = (float) json.getDouble(key);
                            break;
                        case STRING:
                            value = json.getString(key);
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    if (!setting.getObjectValue().equals(value)) {
                        rebootSettingChanged |= setting.rebootApp;
                        setting.saveValue(value);
                    }
                    numberOfSettingsImported++;
                } else if (setting.includeWithImportExport() && !setting.isSetToDefault()) {
                    LogHelper.printDebug(() -> "Resetting to default: " + setting);
                    rebootSettingChanged |= setting.rebootApp;
                    setting.saveValue(setting.defaultValue);
                }
            }
            numberOfSettingsImported += SponsorBlockSettings.importCategoriesFromFlatJson(json);

            ReVancedUtils.showToastLong(numberOfSettingsImported == 0
                    ? str("revanced_settings_import_reset")
                    : str("revanced_settings_import_success", numberOfSettingsImported));

            return rebootSettingChanged;
        } catch (JSONException | IllegalArgumentException ex) {
            ReVancedUtils.showToastLong(str("revanced_settings_import_failure_parse", ex.getMessage()));
            LogHelper.printInfo(() -> "", ex);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Import failure: " + ex.getMessage(), ex); // should never happen
        }
        return false;
    }

    // End import / export

    public enum ReturnType {
        BOOLEAN,
        INTEGER,
        LONG,
        FLOAT,
        STRING;

        public void validate(@Nullable Object obj) throws IllegalArgumentException {
            if (!matches(obj)) {
                throw new IllegalArgumentException("'" + obj + "' does not match:" + this);
            }
        }

        public boolean matches(@Nullable Object obj) {
            switch (this) {
                case BOOLEAN:
                    return obj instanceof Boolean;
                case INTEGER:
                    return obj instanceof Integer;
                case LONG:
                    return obj instanceof Long;
                case FLOAT:
                    return obj instanceof Float;
                case STRING:
                    return obj instanceof String;
            }
            return false;
        }
    }
}
