package app.revanced.integrations.settings;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.StringRef;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static app.revanced.integrations.settings.SettingsEnum.ReturnType.*;
import static app.revanced.integrations.settings.SharedPrefCategory.RETURN_YOUTUBE_DISLIKE;
import static app.revanced.integrations.settings.SharedPrefCategory.SPONSOR_BLOCK;
import static app.revanced.integrations.utils.StringRef.str;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public enum SettingsEnum {
    // External downloader
    EXTERNAL_DOWNLOADER("revanced_external_downloader", BOOLEAN, FALSE),
    EXTERNAL_DOWNLOADER_PACKAGE_NAME("revanced_external_downloader_name", STRING,
            "org.schabi.newpipe" /* NewPipe */, parents(EXTERNAL_DOWNLOADER)),

    // Copy video URL
    COPY_VIDEO_URL("revanced_copy_video_url", BOOLEAN, FALSE),
    COPY_VIDEO_URL_TIMESTAMP("revanced_copy_video_url_timestamp", BOOLEAN, TRUE),

    // Video
    HDR_AUTO_BRIGHTNESS("revanced_hdr_auto_brightness", BOOLEAN, TRUE),
    @Deprecated SHOW_OLD_VIDEO_QUALITY_MENU("revanced_show_old_video_quality_menu", BOOLEAN, TRUE),
    RESTORE_OLD_VIDEO_QUALITY_MENU("revanced_restore_old_video_quality_menu", BOOLEAN, TRUE),
    REMEMBER_VIDEO_QUALITY_LAST_SELECTED("revanced_remember_video_quality_last_selected", BOOLEAN, TRUE),
    VIDEO_QUALITY_DEFAULT_WIFI("revanced_video_quality_default_wifi", INTEGER, -2),
    VIDEO_QUALITY_DEFAULT_MOBILE("revanced_video_quality_default_mobile", INTEGER, -2),
    REMEMBER_PLAYBACK_SPEED_LAST_SELECTED("revanced_remember_playback_speed_last_selected", BOOLEAN, TRUE),
    PLAYBACK_SPEED_DEFAULT("revanced_playback_speed_default", FLOAT, 1.0f),
    CUSTOM_PLAYBACK_SPEEDS("revanced_custom_playback_speeds", STRING,
            "0.25\n0.5\n0.75\n0.9\n0.95\n1.0\n1.05\n1.1\n1.25\n1.5\n1.75\n2.0\n3.0\n4.0\n5.0", true),

    // Ads
    HIDE_BUTTONED_ADS("revanced_hide_buttoned_ads", BOOLEAN, TRUE),
    HIDE_GENERAL_ADS("revanced_hide_general_ads", BOOLEAN, TRUE),
    HIDE_GET_PREMIUM("revanced_hide_get_premium", BOOLEAN, TRUE),
    HIDE_HIDE_LATEST_POSTS("revanced_hide_latest_posts_ads", BOOLEAN, TRUE),
    HIDE_MERCHANDISE_BANNERS("revanced_hide_merchandise_banners", BOOLEAN, TRUE),
    HIDE_PAID_CONTENT("revanced_hide_paid_content_ads", BOOLEAN, TRUE),
    HIDE_PRODUCTS_BANNER("revanced_hide_products_banner", BOOLEAN, TRUE),
    HIDE_SHOPPING_LINKS("revanced_hide_shopping_links", BOOLEAN, TRUE),
    HIDE_SELF_SPONSOR("revanced_hide_self_sponsor_ads", BOOLEAN, TRUE),
    HIDE_VIDEO_ADS("revanced_hide_video_ads", BOOLEAN, TRUE, true),
    HIDE_WEB_SEARCH_RESULTS("revanced_hide_web_search_results", BOOLEAN, TRUE),

    // Layout
    ALT_THUMBNAIL_STILLS("revanced_alt_thumbnail_stills", BOOLEAN, FALSE),
    ALT_THUMBNAIL_STILLS_TIME("revanced_alt_thumbnail_stills_time", INTEGER, 2, parents(ALT_THUMBNAIL_STILLS)),
    ALT_THUMBNAIL_STILLS_FAST("revanced_alt_thumbnail_stills_fast", BOOLEAN, FALSE, parents(ALT_THUMBNAIL_STILLS)),
    ALT_THUMBNAIL_DEARROW("revanced_alt_thumbnail_dearrow", BOOLEAN, false),
    ALT_THUMBNAIL_DEARROW_API_URL("revanced_alt_thumbnail_dearrow_api_url", STRING,
            "https://dearrow-thumb.ajay.app/api/v1/getThumbnail", true, parents(ALT_THUMBNAIL_DEARROW)),
    ALT_THUMBNAIL_DEARROW_CONNECTION_TOAST("revanced_alt_thumbnail_dearrow_connection_toast", BOOLEAN, TRUE, parents(ALT_THUMBNAIL_DEARROW)),
    CUSTOM_FILTER("revanced_custom_filter", BOOLEAN, FALSE),
    CUSTOM_FILTER_STRINGS("revanced_custom_filter_strings", STRING, "", true, parents(CUSTOM_FILTER)),
    DISABLE_FULLSCREEN_AMBIENT_MODE("revanced_disable_fullscreen_ambient_mode", BOOLEAN, TRUE, true),
    DISABLE_RESUMING_SHORTS_PLAYER("revanced_disable_resuming_shorts_player", BOOLEAN, FALSE),
    DISABLE_ROLLING_NUMBER_ANIMATIONS("revanced_disable_rolling_number_animations", BOOLEAN, FALSE),
    DISABLE_SUGGESTED_VIDEO_END_SCREEN("revanced_disable_suggested_video_end_screen", BOOLEAN, TRUE),
    GRADIENT_LOADING_SCREEN("revanced_gradient_loading_screen", BOOLEAN, FALSE),
    HIDE_ALBUM_CARDS("revanced_hide_album_cards", BOOLEAN, FALSE, true),
    HIDE_ARTIST_CARDS("revanced_hide_artist_cards", BOOLEAN, FALSE),
    HIDE_AUTOPLAY_BUTTON("revanced_hide_autoplay_button", BOOLEAN, TRUE, true),
    HIDE_BREAKING_NEWS("revanced_hide_breaking_news", BOOLEAN, TRUE, true),
    HIDE_CAPTIONS_BUTTON("revanced_hide_captions_button", BOOLEAN, FALSE),
    HIDE_CAST_BUTTON("revanced_hide_cast_button", BOOLEAN, TRUE, true),
    HIDE_CHANNEL_BAR("revanced_hide_channel_bar", BOOLEAN, FALSE),
    HIDE_CHANNEL_MEMBER_SHELF("revanced_hide_channel_member_shelf", BOOLEAN, TRUE),
    HIDE_CHIPS_SHELF("revanced_hide_chips_shelf", BOOLEAN, TRUE),
    HIDE_COMMENTS_SECTION("revanced_hide_comments_section", BOOLEAN, FALSE, true),
    HIDE_COMMUNITY_GUIDELINES("revanced_hide_community_guidelines", BOOLEAN, TRUE),
    HIDE_COMMUNITY_POSTS("revanced_hide_community_posts", BOOLEAN, FALSE),
    HIDE_COMPACT_BANNER("revanced_hide_compact_banner", BOOLEAN, TRUE),
    HIDE_CREATE_BUTTON("revanced_hide_create_button", BOOLEAN, TRUE, true),
    HIDE_CROWDFUNDING_BOX("revanced_hide_crowdfunding_box", BOOLEAN, FALSE, true),
    HIDE_EMAIL_ADDRESS("revanced_hide_email_address", BOOLEAN, FALSE),
    HIDE_EMERGENCY_BOX("revanced_hide_emergency_box", BOOLEAN, TRUE),
    HIDE_ENDSCREEN_CARDS("revanced_hide_endscreen_cards", BOOLEAN, TRUE),
    HIDE_EXPANDABLE_CHIP("revanced_hide_expandable_chip", BOOLEAN, TRUE),
    HIDE_FEED_SURVEY("revanced_hide_feed_survey", BOOLEAN, TRUE),
    HIDE_FILTER_BAR_FEED_IN_FEED("revanced_hide_filter_bar_feed_in_feed", BOOLEAN, FALSE, true),
    HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS("revanced_hide_filter_bar_feed_in_related_videos", BOOLEAN, FALSE, true),
    HIDE_FILTER_BAR_FEED_IN_SEARCH("revanced_hide_filter_bar_feed_in_search", BOOLEAN, FALSE, true),
    HIDE_FLOATING_MICROPHONE_BUTTON("revanced_hide_floating_microphone_button", BOOLEAN, TRUE, true),
    HIDE_FULLSCREEN_PANELS("revanced_hide_fullscreen_panels", BOOLEAN, TRUE, true),
    HIDE_GRAY_SEPARATOR("revanced_hide_gray_separator", BOOLEAN, TRUE),
    HIDE_HIDE_CHANNEL_GUIDELINES("revanced_hide_channel_guidelines", BOOLEAN, TRUE),
    HIDE_HIDE_INFO_PANELS("revanced_hide_info_panels", BOOLEAN, TRUE),
    HIDE_HOME_BUTTON("revanced_hide_home_button", BOOLEAN, FALSE, true),
    HIDE_IMAGE_SHELF("revanced_hide_image_shelf", BOOLEAN, TRUE),
    HIDE_INFO_CARDS("revanced_hide_info_cards", BOOLEAN, TRUE),
    HIDE_JOIN_MEMBERSHIP_BUTTON("revanced_hide_join_membership_button", BOOLEAN, TRUE),
    HIDE_LOAD_MORE_BUTTON("revanced_hide_load_more_button", BOOLEAN, TRUE, true),
    HIDE_MEDICAL_PANELS("revanced_hide_medical_panels", BOOLEAN, TRUE),
    HIDE_MIX_PLAYLISTS("revanced_hide_mix_playlists", BOOLEAN, TRUE),
    HIDE_MOVIES_SECTION("revanced_hide_movies_section", BOOLEAN, TRUE),
    HIDE_NOTIFY_ME_BUTTON("revanced_hide_notify_me_button", BOOLEAN, TRUE),
    HIDE_PLAYER_BUTTONS("revanced_hide_player_buttons", BOOLEAN, FALSE),
    HIDE_PREVIEW_COMMENT("revanced_hide_preview_comment", BOOLEAN, FALSE, true),
    HIDE_QUICK_ACTIONS("revanced_hide_quick_actions", BOOLEAN, FALSE),
    HIDE_RELATED_VIDEOS("revanced_hide_related_videos", BOOLEAN, FALSE),
    HIDE_SEARCH_RESULT_SHELF_HEADER("revanced_hide_search_result_shelf_header", BOOLEAN, FALSE),
    HIDE_SHORTS_BUTTON("revanced_hide_shorts_button", BOOLEAN, TRUE, true),
    HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES("revanced_hide_subscribers_community_guidelines", BOOLEAN, TRUE),
    HIDE_SUBSCRIPTIONS_BUTTON("revanced_hide_subscriptions_button", BOOLEAN, FALSE, true),
    HIDE_TIMED_REACTIONS("revanced_hide_timed_reactions", BOOLEAN, TRUE),
    HIDE_TIMESTAMP("revanced_hide_timestamp", BOOLEAN, FALSE),
    @Deprecated HIDE_VIDEO_WATERMARK("revanced_hide_video_watermark", BOOLEAN, TRUE),
    HIDE_VIDEO_CHANNEL_WATERMARK("revanced_hide_channel_watermark", BOOLEAN, TRUE),
    HIDE_FOR_YOU_SHELF("revanced_hide_for_you_shelf", BOOLEAN, TRUE),
    HIDE_VIDEO_QUALITY_MENU_FOOTER("revanced_hide_video_quality_menu_footer", BOOLEAN, TRUE),
    HIDE_SEARCH_RESULT_RECOMMENDATIONS("revanced_hide_search_result_recommendations", BOOLEAN, TRUE),
    PLAYER_OVERLAY_OPACITY("revanced_player_overlay_opacity", INTEGER, 100, true),
    PLAYER_POPUP_PANELS("revanced_hide_player_popup_panels", BOOLEAN, FALSE),
    SPOOF_APP_VERSION("revanced_spoof_app_version", BOOLEAN, FALSE, true, "revanced_spoof_app_version_user_dialog_message"),
    SPOOF_APP_VERSION_TARGET("revanced_spoof_app_version_target", STRING, "17.08.35", true, parents(SPOOF_APP_VERSION)),
    SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON("revanced_switch_create_with_notifications_button", BOOLEAN, TRUE, true),
    TABLET_LAYOUT("revanced_tablet_layout", BOOLEAN, FALSE, true, "revanced_tablet_layout_user_dialog_message"),
    USE_TABLET_MINIPLAYER("revanced_tablet_miniplayer", BOOLEAN, FALSE, true),
    WIDE_SEARCHBAR("revanced_wide_searchbar", BOOLEAN, FALSE, true),
    START_PAGE("revanced_start_page", STRING, ""),

    // Description
    HIDE_CHAPTERS("revanced_hide_chapters", BOOLEAN, TRUE),
    HIDE_INFO_CARDS_SECTION("revanced_hide_info_cards_section", BOOLEAN, TRUE),
    HIDE_GAME_SECTION("revanced_hide_game_section", BOOLEAN, TRUE),
    HIDE_MUSIC_SECTION("revanced_hide_music_section", BOOLEAN, TRUE),
    HIDE_PODCAST_SECTION("revanced_hide_podcast_section", BOOLEAN, TRUE),
    HIDE_TRANSCIPT_SECTION("revanced_hide_transcript_section", BOOLEAN, TRUE),

    // Shorts
    HIDE_SHORTS("revanced_hide_shorts", BOOLEAN, FALSE, true),
    HIDE_SHORTS_JOIN_BUTTON("revanced_hide_shorts_join_button", BOOLEAN, TRUE),
    HIDE_SHORTS_SUBSCRIBE_BUTTON("revanced_hide_shorts_subscribe_button", BOOLEAN, TRUE),
    HIDE_SHORTS_SUBSCRIBE_BUTTON_PAUSED("revanced_hide_shorts_subscribe_button_paused", BOOLEAN, FALSE),
    HIDE_SHORTS_THANKS_BUTTON("revanced_hide_shorts_thanks_button", BOOLEAN, TRUE),
    HIDE_SHORTS_COMMENTS_BUTTON("revanced_hide_shorts_comments_button", BOOLEAN, FALSE),
    HIDE_SHORTS_REMIX_BUTTON("revanced_hide_shorts_remix_button", BOOLEAN, TRUE),
    HIDE_SHORTS_SHARE_BUTTON("revanced_hide_shorts_share_button", BOOLEAN, FALSE),
    HIDE_SHORTS_INFO_PANEL("revanced_hide_shorts_info_panel", BOOLEAN, TRUE),
    HIDE_SHORTS_SOUND_BUTTON("revanced_hide_shorts_sound_button", BOOLEAN, FALSE),
    HIDE_SHORTS_CHANNEL_BAR("revanced_hide_shorts_channel_bar", BOOLEAN, FALSE),
    HIDE_SHORTS_NAVIGATION_BAR("revanced_hide_shorts_navigation_bar", BOOLEAN, TRUE, true),

    // Seekbar
    @Deprecated ENABLE_OLD_SEEKBAR_THUMBNAILS("revanced_enable_old_seekbar_thumbnails", BOOLEAN, TRUE),
    RESTORE_OLD_SEEKBAR_THUMBNAILS("revanced_restore_old_seekbar_thumbnails", BOOLEAN, TRUE),
    HIDE_SEEKBAR("revanced_hide_seekbar", BOOLEAN, FALSE),
    HIDE_SEEKBAR_THUMBNAIL("revanced_hide_seekbar_thumbnail", BOOLEAN, FALSE),
    SEEKBAR_CUSTOM_COLOR("revanced_seekbar_custom_color", BOOLEAN, TRUE, true),
    SEEKBAR_CUSTOM_COLOR_VALUE("revanced_seekbar_custom_color_value", STRING, "#FF0000", true, parents(SEEKBAR_CUSTOM_COLOR)),

    // Action buttons
    HIDE_LIKE_DISLIKE_BUTTON("revanced_hide_like_dislike_button", BOOLEAN, FALSE),
    HIDE_LIVE_CHAT_BUTTON("revanced_hide_live_chat_button", BOOLEAN, FALSE),
    HIDE_SHARE_BUTTON("revanced_hide_share_button", BOOLEAN, FALSE),
    HIDE_REPORT_BUTTON("revanced_hide_report_button", BOOLEAN, FALSE),
    HIDE_REMIX_BUTTON("revanced_hide_remix_button", BOOLEAN, TRUE),
    HIDE_DOWNLOAD_BUTTON("revanced_hide_download_button", BOOLEAN, FALSE),
    HIDE_THANKS_BUTTON("revanced_hide_thanks_button", BOOLEAN, TRUE),
    HIDE_CLIP_BUTTON("revanced_hide_clip_button", BOOLEAN, TRUE),
    HIDE_PLAYLIST_BUTTON("revanced_hide_playlist_button", BOOLEAN, FALSE),
    HIDE_SHOP_BUTTON("revanced_hide_shop_button", BOOLEAN, TRUE),

    // Player flyout menu items
    HIDE_CAPTIONS_MENU("revanced_hide_player_flyout_captions", BOOLEAN, FALSE),
    HIDE_ADDITIONAL_SETTINGS_MENU("revanced_hide_player_flyout_additional_settings", BOOLEAN, FALSE),
    HIDE_LOOP_VIDEO_MENU("revanced_hide_player_flyout_loop_video", BOOLEAN, FALSE),
    HIDE_AMBIENT_MODE_MENU("revanced_hide_player_flyout_ambient_mode", BOOLEAN, FALSE),
    HIDE_REPORT_MENU("revanced_hide_player_flyout_report", BOOLEAN, TRUE),
    HIDE_HELP_MENU("revanced_hide_player_flyout_help", BOOLEAN, TRUE),
    HIDE_SPEED_MENU("revanced_hide_player_flyout_speed", BOOLEAN, FALSE),
    HIDE_MORE_INFO_MENU("revanced_hide_player_flyout_more_info", BOOLEAN, TRUE),
    HIDE_AUDIO_TRACK_MENU("revanced_hide_player_flyout_audio_track", BOOLEAN, FALSE),
    HIDE_WATCH_IN_VR_MENU("revanced_hide_player_flyout_watch_in_vr", BOOLEAN, TRUE),

    // Misc
    AUTO_CAPTIONS("revanced_auto_captions", BOOLEAN, FALSE),
    DISABLE_ZOOM_HAPTICS("revanced_disable_zoom_haptics", BOOLEAN, TRUE),
    EXTERNAL_BROWSER("revanced_external_browser", BOOLEAN, TRUE, true),
    AUTO_REPEAT("revanced_auto_repeat", BOOLEAN, FALSE),
    SEEKBAR_TAPPING("revanced_seekbar_tapping", BOOLEAN, TRUE),
    SLIDE_TO_SEEK("revanced_slide_to_seek", BOOLEAN, FALSE),
    @Deprecated DISABLE_FINE_SCRUBBING_GESTURE("revanced_disable_fine_scrubbing_gesture", BOOLEAN, TRUE),
    DISABLE_PRECISE_SEEKING_GESTURE("revanced_disable_precise_seeking_gesture", BOOLEAN, TRUE),
    SPOOF_SIGNATURE("revanced_spoof_signature_verification_enabled", BOOLEAN, TRUE, true,
            "revanced_spoof_signature_verification_enabled_user_dialog_message"),
    SPOOF_SIGNATURE_IN_FEED("revanced_spoof_signature_in_feed_enabled", BOOLEAN, FALSE, false,
            parents(SPOOF_SIGNATURE)),
    SPOOF_STORYBOARD_RENDERER("revanced_spoof_storyboard", BOOLEAN, TRUE, true,
            parents(SPOOF_SIGNATURE)),

    SPOOF_DEVICE_DIMENSIONS("revanced_spoof_device_dimensions", BOOLEAN, FALSE, true),
    BYPASS_URL_REDIRECTS("revanced_bypass_url_redirects", BOOLEAN, TRUE),
    ANNOUNCEMENTS("revanced_announcements", BOOLEAN, TRUE),
    ANNOUNCEMENT_CONSUMER("revanced_announcement_consumer", STRING, ""),
    ANNOUNCEMENT_LAST_HASH("revanced_announcement_last_hash", STRING, ""),
    REMOVE_TRACKING_QUERY_PARAMETER("revanced_remove_tracking_query_parameter", BOOLEAN, TRUE),

    // Swipe controls
    SWIPE_BRIGHTNESS("revanced_swipe_brightness", BOOLEAN, TRUE),
    SWIPE_VOLUME("revanced_swipe_volume", BOOLEAN, TRUE),
    SWIPE_PRESS_TO_ENGAGE("revanced_swipe_press_to_engage", BOOLEAN, FALSE, true,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_HAPTIC_FEEDBACK("revanced_swipe_haptic_feedback", BOOLEAN, TRUE, true,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_MAGNITUDE_THRESHOLD("revanced_swipe_threshold", INTEGER, 30, true,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_OVERLAY_BACKGROUND_ALPHA("revanced_swipe_overlay_background_alpha", INTEGER, 127, true,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_OVERLAY_TEXT_SIZE("revanced_swipe_text_overlay_size", INTEGER, 22, true,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_OVERLAY_TIMEOUT("revanced_swipe_overlay_timeout", LONG, 500L, true,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),
    SWIPE_SAVE_AND_RESTORE_BRIGHTNESS("revanced_swipe_save_and_restore_brightness", BOOLEAN, TRUE, true,
            parents(SWIPE_BRIGHTNESS, SWIPE_VOLUME)),

    // Debugging
    DEBUG("revanced_debug", BOOLEAN, FALSE),
    DEBUG_STACKTRACE("revanced_debug_stacktrace", BOOLEAN, FALSE, parents(DEBUG)),
    DEBUG_PROTOBUFFER("revanced_debug_protobuffer", BOOLEAN, FALSE, parents(DEBUG)),
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
    SB_LOCAL_TIME_SAVED_MILLISECONDS("sb_local_time_saved_milliseconds", LONG, 0L, SPONSOR_BLOCK);

    private static SettingsEnum[] parents(SettingsEnum... parents) {
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
        this(path, returnType, defaultValue, SharedPrefCategory.YOUTUBE, rebootApp, null, null);
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
                 boolean rebootApp, @Nullable String userDialogMessage, @Nullable SettingsEnum[] parents) {
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

        // region Migration

        migrateOldSettingToNew(HIDE_VIDEO_WATERMARK, HIDE_VIDEO_CHANNEL_WATERMARK);
        migrateOldSettingToNew(DISABLE_FINE_SCRUBBING_GESTURE, DISABLE_PRECISE_SEEKING_GESTURE);
        migrateOldSettingToNew(SHOW_OLD_VIDEO_QUALITY_MENU, RESTORE_OLD_VIDEO_QUALITY_MENU);
        migrateOldSettingToNew(ENABLE_OLD_SEEKBAR_THUMBNAILS, RESTORE_OLD_SEEKBAR_THUMBNAILS);

        // Do _not_ delete this SB private user id migration property until sometime in 2024.
        // This is the only setting that cannot be reconfigured if lost,
        // and more time should be given for users who rarely upgrade.
        migrateOldSettingToNew(DEPRECATED_SB_UUID_OLD_MIGRATION_SETTING, SB_PRIVATE_USER_ID);

        // This migration may need to remain here for a while.
        // Older online guides will still reference using commas,
        // and this code will automatically convert anything the user enters to newline format,
        // and also migrate any imported older settings that using commas.
        String componentsToFilter = SettingsEnum.CUSTOM_FILTER_STRINGS.getString();
        if (componentsToFilter.contains(",")) {
            LogHelper.printInfo(() -> "Migrating custom filter strings to new line format");
            SettingsEnum.CUSTOM_FILTER_STRINGS.saveValue(componentsToFilter.replace(",", "\n"));
        }

        // endregion
    }

    /**
     * Migrate a setting value if the path is renamed but otherwise the old and new settings are identical.
     */
    private static void migrateOldSettingToNew(SettingsEnum oldSetting, SettingsEnum newSetting) {
        if (!oldSetting.isSetToDefault()) {
            LogHelper.printInfo(() -> "Migrating old setting of '" + oldSetting.value
                    + "' from: " + oldSetting + " into replacement setting: " + newSetting);
            newSetting.saveValue(oldSetting.value);
            oldSetting.resetToDefault();
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
     * <p>
     * This intentionally is a static method, to deter accidental usage
     * when {@link #saveValue(Object)} was intended.
     * <p>
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
     * Identical to calling {@link #saveValue(Object)} using {@link #defaultValue}.
     */
    public void resetToDefault() {
        saveValue(defaultValue);
    }

    /**
     * @return if this setting can be configured and used.
     * <p>
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
            case ANNOUNCEMENT_CONSUMER: // Not useful to export, no reason to include it.
            case SB_LAST_VIP_CHECK:
            case SB_HIDE_EXPORT_WARNING:
            case SB_SEEN_GUIDELINES:
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
                    setting.resetToDefault();
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
