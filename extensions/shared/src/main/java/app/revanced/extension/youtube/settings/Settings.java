package app.revanced.extension.youtube.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.extension.shared.settings.Setting.*;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerType;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerType.MODERN_1;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerType.MODERN_3;
import static app.revanced.extension.youtube.patches.spoof.SpoofClientPatch.ClientType;
import static app.revanced.extension.youtube.sponsorblock.objects.CategoryBehaviour.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.*;
import app.revanced.extension.shared.settings.preference.SharedPrefCategory;
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.DeArrowAvailability;
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.StillImagesAvailability;
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.ThumbnailOption;
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.ThumbnailStillTime;
import app.revanced.extension.youtube.patches.spoof.SpoofAppVersionPatch;
import app.revanced.extension.youtube.patches.spoof.SpoofClientPatch;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings;

@SuppressWarnings("deprecation")
public class Settings extends BaseSettings {
    // Video
    public static final BooleanSetting RESTORE_OLD_VIDEO_QUALITY_MENU = new BooleanSetting("revanced_restore_old_video_quality_menu", TRUE);
    public static final BooleanSetting REMEMBER_VIDEO_QUALITY_LAST_SELECTED = new BooleanSetting("revanced_remember_video_quality_last_selected", FALSE);
    public static final IntegerSetting VIDEO_QUALITY_DEFAULT_WIFI = new IntegerSetting("revanced_video_quality_default_wifi", -2);
    public static final IntegerSetting VIDEO_QUALITY_DEFAULT_MOBILE = new IntegerSetting("revanced_video_quality_default_mobile", -2);
    public static final BooleanSetting REMEMBER_PLAYBACK_SPEED_LAST_SELECTED = new BooleanSetting("revanced_remember_playback_speed_last_selected", FALSE);
    public static final FloatSetting PLAYBACK_SPEED_DEFAULT = new FloatSetting("revanced_playback_speed_default", 1.0f);
    public static final StringSetting CUSTOM_PLAYBACK_SPEEDS = new StringSetting("revanced_custom_playback_speeds",
            "0.25\n0.5\n0.75\n0.9\n0.95\n1.0\n1.05\n1.1\n1.25\n1.5\n1.75\n2.0\n3.0\n4.0\n5.0", true);
    @Deprecated // Patch is obsolete and no longer works with 19.09+
    public static final BooleanSetting HDR_AUTO_BRIGHTNESS = new BooleanSetting("revanced_hdr_auto_brightness", TRUE);

    // Ads
    public static final BooleanSetting HIDE_FULLSCREEN_ADS = new BooleanSetting("revanced_hide_fullscreen_ads", TRUE);
    public static final BooleanSetting HIDE_BUTTONED_ADS = new BooleanSetting("revanced_hide_buttoned_ads", TRUE);
    public static final BooleanSetting HIDE_GENERAL_ADS = new BooleanSetting("revanced_hide_general_ads", TRUE);
    public static final BooleanSetting HIDE_GET_PREMIUM = new BooleanSetting("revanced_hide_get_premium", TRUE);
    public static final BooleanSetting HIDE_HIDE_LATEST_POSTS = new BooleanSetting("revanced_hide_latest_posts_ads", TRUE);
    public static final BooleanSetting HIDE_MERCHANDISE_BANNERS = new BooleanSetting("revanced_hide_merchandise_banners", TRUE);
    public static final BooleanSetting HIDE_PAID_PROMOTION_LABEL = new BooleanSetting("revanced_hide_paid_promotion_label", TRUE);
    public static final BooleanSetting HIDE_PRODUCTS_BANNER = new BooleanSetting("revanced_hide_products_banner", TRUE);
    public static final BooleanSetting HIDE_SHOPPING_LINKS = new BooleanSetting("revanced_hide_shopping_links", TRUE);
    public static final BooleanSetting HIDE_SELF_SPONSOR = new BooleanSetting("revanced_hide_self_sponsor_ads", TRUE);
    public static final BooleanSetting HIDE_VIDEO_ADS = new BooleanSetting("revanced_hide_video_ads", TRUE, true);
    public static final BooleanSetting HIDE_VISIT_STORE_BUTTON = new BooleanSetting("revanced_hide_visit_store_button", TRUE);
    public static final BooleanSetting HIDE_WEB_SEARCH_RESULTS = new BooleanSetting("revanced_hide_web_search_results", TRUE);

    // Feed
    public static final BooleanSetting HIDE_ALBUM_CARDS = new BooleanSetting("revanced_hide_album_cards", FALSE, true);
    public static final BooleanSetting HIDE_ARTIST_CARDS = new BooleanSetting("revanced_hide_artist_cards", FALSE);
    public static final BooleanSetting HIDE_EXPANDABLE_CHIP = new BooleanSetting("revanced_hide_expandable_chip", TRUE);

    // Alternative thumbnails
    public static final EnumSetting<ThumbnailOption> ALT_THUMBNAIL_HOME = new EnumSetting<>("revanced_alt_thumbnail_home", ThumbnailOption.ORIGINAL);
    public static final EnumSetting<ThumbnailOption> ALT_THUMBNAIL_SUBSCRIPTIONS = new EnumSetting<>("revanced_alt_thumbnail_subscription", ThumbnailOption.ORIGINAL);
    public static final EnumSetting<ThumbnailOption> ALT_THUMBNAIL_LIBRARY = new EnumSetting<>("revanced_alt_thumbnail_library", ThumbnailOption.ORIGINAL);
    public static final EnumSetting<ThumbnailOption> ALT_THUMBNAIL_PLAYER = new EnumSetting<>("revanced_alt_thumbnail_player", ThumbnailOption.ORIGINAL);
    public static final EnumSetting<ThumbnailOption> ALT_THUMBNAIL_SEARCH = new EnumSetting<>("revanced_alt_thumbnail_search", ThumbnailOption.ORIGINAL);
    public static final StringSetting ALT_THUMBNAIL_DEARROW_API_URL = new StringSetting("revanced_alt_thumbnail_dearrow_api_url",
            "https://dearrow-thumb.ajay.app/api/v1/getThumbnail", true, new DeArrowAvailability());
    public static final BooleanSetting ALT_THUMBNAIL_DEARROW_CONNECTION_TOAST = new BooleanSetting("revanced_alt_thumbnail_dearrow_connection_toast", TRUE, new DeArrowAvailability());
    public static final EnumSetting<ThumbnailStillTime> ALT_THUMBNAIL_STILLS_TIME = new EnumSetting<>("revanced_alt_thumbnail_stills_time", ThumbnailStillTime.MIDDLE, new StillImagesAvailability());
    public static final BooleanSetting ALT_THUMBNAIL_STILLS_FAST = new BooleanSetting("revanced_alt_thumbnail_stills_fast", FALSE, new StillImagesAvailability());

    // Hide keyword content
    public static final BooleanSetting HIDE_KEYWORD_CONTENT_HOME = new BooleanSetting("revanced_hide_keyword_content_home", FALSE);
    public static final BooleanSetting HIDE_KEYWORD_CONTENT_SUBSCRIPTIONS = new BooleanSetting("revanced_hide_keyword_content_subscriptions", FALSE);
    public static final BooleanSetting HIDE_KEYWORD_CONTENT_SEARCH = new BooleanSetting("revanced_hide_keyword_content_search", FALSE);
    public static final StringSetting HIDE_KEYWORD_CONTENT_PHRASES = new StringSetting("revanced_hide_keyword_content_phrases", "",
            parentsAny(HIDE_KEYWORD_CONTENT_HOME, HIDE_KEYWORD_CONTENT_SUBSCRIPTIONS, HIDE_KEYWORD_CONTENT_SEARCH));

    // Uncategorized layout related settings.  Do not add to this section, and instead move these out and categorize them.
    public static final BooleanSetting DISABLE_SUGGESTED_VIDEO_END_SCREEN = new BooleanSetting("revanced_disable_suggested_video_end_screen", FALSE, true);
    public static final BooleanSetting GRADIENT_LOADING_SCREEN = new BooleanSetting("revanced_gradient_loading_screen", FALSE);
    public static final BooleanSetting HIDE_HORIZONTAL_SHELVES = new BooleanSetting("revanced_hide_horizontal_shelves", TRUE);
    public static final BooleanSetting HIDE_CAPTIONS_BUTTON = new BooleanSetting("revanced_hide_captions_button", FALSE);
    public static final BooleanSetting HIDE_CHANNEL_BAR = new BooleanSetting("revanced_hide_channel_bar", FALSE);
    public static final BooleanSetting HIDE_CHANNEL_MEMBER_SHELF = new BooleanSetting("revanced_hide_channel_member_shelf", TRUE);
    public static final BooleanSetting HIDE_CHIPS_SHELF = new BooleanSetting("revanced_hide_chips_shelf", TRUE);
    public static final BooleanSetting HIDE_COMMUNITY_GUIDELINES = new BooleanSetting("revanced_hide_community_guidelines", TRUE);
    public static final BooleanSetting HIDE_COMMUNITY_POSTS = new BooleanSetting("revanced_hide_community_posts", FALSE);
    public static final BooleanSetting HIDE_COMPACT_BANNER = new BooleanSetting("revanced_hide_compact_banner", TRUE);
    public static final BooleanSetting HIDE_CROWDFUNDING_BOX = new BooleanSetting("revanced_hide_crowdfunding_box", FALSE, true);
    @Deprecated public static final BooleanSetting HIDE_EMAIL_ADDRESS = new BooleanSetting("revanced_hide_email_address", FALSE);
    public static final BooleanSetting HIDE_EMERGENCY_BOX = new BooleanSetting("revanced_hide_emergency_box", TRUE);
    public static final BooleanSetting HIDE_ENDSCREEN_CARDS = new BooleanSetting("revanced_hide_endscreen_cards", FALSE);
    public static final BooleanSetting HIDE_FEED_SURVEY = new BooleanSetting("revanced_hide_feed_survey", TRUE);
    public static final BooleanSetting HIDE_FILTER_BAR_FEED_IN_FEED = new BooleanSetting("revanced_hide_filter_bar_feed_in_feed", FALSE, true);
    public static final BooleanSetting HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS = new BooleanSetting("revanced_hide_filter_bar_feed_in_related_videos", FALSE, true);
    public static final BooleanSetting HIDE_FILTER_BAR_FEED_IN_SEARCH = new BooleanSetting("revanced_hide_filter_bar_feed_in_search", FALSE, true);
    public static final BooleanSetting HIDE_FLOATING_MICROPHONE_BUTTON = new BooleanSetting("revanced_hide_floating_microphone_button", TRUE, true);
    public static final BooleanSetting HIDE_FULLSCREEN_PANELS = new BooleanSetting("revanced_hide_fullscreen_panels", TRUE, true);
    public static final BooleanSetting HIDE_GRAY_SEPARATOR = new BooleanSetting("revanced_hide_gray_separator", TRUE);
    public static final BooleanSetting HIDE_HIDE_CHANNEL_GUIDELINES = new BooleanSetting("revanced_hide_channel_guidelines", TRUE);
    public static final BooleanSetting HIDE_HIDE_INFO_PANELS = new BooleanSetting("revanced_hide_info_panels", TRUE);
    public static final BooleanSetting HIDE_IMAGE_SHELF = new BooleanSetting("revanced_hide_image_shelf", TRUE);
    public static final BooleanSetting HIDE_INFO_CARDS = new BooleanSetting("revanced_hide_info_cards", FALSE);
    public static final BooleanSetting HIDE_JOIN_MEMBERSHIP_BUTTON = new BooleanSetting("revanced_hide_join_membership_button", TRUE);
    @Deprecated public static final BooleanSetting HIDE_LOAD_MORE_BUTTON = new BooleanSetting("revanced_hide_load_more_button", TRUE);
    public static final BooleanSetting HIDE_SHOW_MORE_BUTTON = new BooleanSetting("revanced_hide_show_more_button", TRUE, true);
    public static final BooleanSetting HIDE_MEDICAL_PANELS = new BooleanSetting("revanced_hide_medical_panels", TRUE);
    public static final BooleanSetting HIDE_MIX_PLAYLISTS = new BooleanSetting("revanced_hide_mix_playlists", TRUE);
    public static final BooleanSetting HIDE_MOVIES_SECTION = new BooleanSetting("revanced_hide_movies_section", TRUE);
    public static final BooleanSetting HIDE_NOTIFY_ME_BUTTON = new BooleanSetting("revanced_hide_notify_me_button", TRUE);
    public static final BooleanSetting HIDE_PLAYABLES = new BooleanSetting("revanced_hide_playables", TRUE);
    public static final BooleanSetting HIDE_QUICK_ACTIONS = new BooleanSetting("revanced_hide_quick_actions", FALSE);
    public static final BooleanSetting HIDE_RELATED_VIDEOS = new BooleanSetting("revanced_hide_related_videos", FALSE);
    public static final BooleanSetting HIDE_SEARCH_RESULT_SHELF_HEADER = new BooleanSetting("revanced_hide_search_result_shelf_header", FALSE);
    public static final BooleanSetting HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES = new BooleanSetting("revanced_hide_subscribers_community_guidelines", TRUE);
    public static final BooleanSetting HIDE_TIMED_REACTIONS = new BooleanSetting("revanced_hide_timed_reactions", TRUE);
    public static final BooleanSetting HIDE_TIMESTAMP = new BooleanSetting("revanced_hide_timestamp", FALSE);
    public static final BooleanSetting HIDE_VIDEO_CHANNEL_WATERMARK = new BooleanSetting("revanced_hide_channel_watermark", TRUE);
    public static final BooleanSetting HIDE_FOR_YOU_SHELF = new BooleanSetting("revanced_hide_for_you_shelf", TRUE);
    public static final BooleanSetting HIDE_SEARCH_RESULT_RECOMMENDATIONS = new BooleanSetting("revanced_hide_search_result_recommendations", TRUE);
    public static final IntegerSetting PLAYER_OVERLAY_OPACITY = new IntegerSetting("revanced_player_overlay_opacity",100, true);
    public static final BooleanSetting PLAYER_POPUP_PANELS = new BooleanSetting("revanced_hide_player_popup_panels", FALSE);

    // Player
    public static final BooleanSetting DISABLE_FULLSCREEN_AMBIENT_MODE = new BooleanSetting("revanced_disable_fullscreen_ambient_mode", TRUE, true);
    public static final BooleanSetting DISABLE_ROLLING_NUMBER_ANIMATIONS = new BooleanSetting("revanced_disable_rolling_number_animations", FALSE);
    public static final BooleanSetting DISABLE_LIKE_SUBSCRIBE_GLOW = new BooleanSetting("revanced_disable_like_subscribe_glow", FALSE);
    public static final BooleanSetting HIDE_AUTOPLAY_BUTTON = new BooleanSetting("revanced_hide_autoplay_button", TRUE, true);
    public static final BooleanSetting HIDE_CAST_BUTTON = new BooleanSetting("revanced_hide_cast_button", TRUE, true);
    public static final BooleanSetting HIDE_PLAYER_BUTTONS = new BooleanSetting("revanced_hide_player_buttons", FALSE);
    public static final BooleanSetting COPY_VIDEO_URL = new BooleanSetting("revanced_copy_video_url", FALSE);
    public static final BooleanSetting COPY_VIDEO_URL_TIMESTAMP = new BooleanSetting("revanced_copy_video_url_timestamp", TRUE);
    public static final BooleanSetting PLAYBACK_SPEED_DIALOG_BUTTON = new BooleanSetting("revanced_playback_speed_dialog_button", FALSE);

    // Miniplayer
    public static final EnumSetting<MiniplayerType> MINIPLAYER_TYPE = new EnumSetting<>("revanced_miniplayer_type", MiniplayerType.ORIGINAL, true);
    public static final BooleanSetting MINIPLAYER_HIDE_EXPAND_CLOSE = new BooleanSetting("revanced_miniplayer_hide_expand_close", FALSE, true, MINIPLAYER_TYPE.availability(MODERN_1, MODERN_3));
    public static final BooleanSetting MINIPLAYER_HIDE_SUBTEXT = new BooleanSetting("revanced_miniplayer_hide_subtext", FALSE, true, MINIPLAYER_TYPE.availability(MODERN_1, MODERN_3));
    public static final BooleanSetting MINIPLAYER_HIDE_REWIND_FORWARD = new BooleanSetting("revanced_miniplayer_hide_rewind_forward", FALSE, true, MINIPLAYER_TYPE.availability(MODERN_1));
    public static final IntegerSetting MINIPLAYER_OPACITY = new IntegerSetting("revanced_miniplayer_opacity", 100, true, MINIPLAYER_TYPE.availability(MODERN_1));

    // External downloader
    public static final BooleanSetting EXTERNAL_DOWNLOADER = new BooleanSetting("revanced_external_downloader", FALSE);
    public static final BooleanSetting EXTERNAL_DOWNLOADER_ACTION_BUTTON = new BooleanSetting("revanced_external_downloader_action_button", FALSE);
    public static final StringSetting EXTERNAL_DOWNLOADER_PACKAGE_NAME = new StringSetting("revanced_external_downloader_name",
            "org.schabi.newpipe" /* NewPipe */, parentsAny(EXTERNAL_DOWNLOADER, EXTERNAL_DOWNLOADER_ACTION_BUTTON));

    // Comments
    public static final BooleanSetting HIDE_COMMENTS_BY_MEMBERS_HEADER = new BooleanSetting("revanced_hide_comments_by_members_header", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_SECTION = new BooleanSetting("revanced_hide_comments_section", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_CREATE_A_SHORT_BUTTON = new BooleanSetting("revanced_hide_comments_create_a_short_button", TRUE);
    public static final BooleanSetting HIDE_COMMENTS_PREVIEW_COMMENT = new BooleanSetting("revanced_hide_comments_preview_comment", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_THANKS_BUTTON = new BooleanSetting("revanced_hide_comments_thanks_button", TRUE);
    public static final BooleanSetting HIDE_COMMENTS_TIMESTAMP_AND_EMOJI_BUTTONS = new BooleanSetting("revanced_hide_comments_timestamp_and_emoji_buttons", TRUE);

    // Description
    public static final BooleanSetting HIDE_ATTRIBUTES_SECTION = new BooleanSetting("revanced_hide_attributes_section", FALSE);
    public static final BooleanSetting HIDE_CHAPTERS_SECTION = new BooleanSetting("revanced_hide_chapters_section", TRUE);
    public static final BooleanSetting HIDE_INFO_CARDS_SECTION = new BooleanSetting("revanced_hide_info_cards_section", TRUE);
    public static final BooleanSetting HIDE_KEY_CONCEPTS_SECTION = new BooleanSetting("revanced_hide_key_concepts_section", FALSE);
    public static final BooleanSetting HIDE_PODCAST_SECTION = new BooleanSetting("revanced_hide_podcast_section", TRUE);
    public static final BooleanSetting HIDE_TRANSCRIPT_SECTION = new BooleanSetting("revanced_hide_transcript_section", TRUE);

    // Action buttons
    public static final BooleanSetting HIDE_LIKE_DISLIKE_BUTTON = new BooleanSetting("revanced_hide_like_dislike_button", FALSE);
    public static final BooleanSetting HIDE_SHARE_BUTTON = new BooleanSetting("revanced_hide_share_button", FALSE);
    public static final BooleanSetting HIDE_REPORT_BUTTON = new BooleanSetting("revanced_hide_report_button", FALSE);
    public static final BooleanSetting HIDE_REMIX_BUTTON = new BooleanSetting("revanced_hide_remix_button", TRUE);
    public static final BooleanSetting HIDE_DOWNLOAD_BUTTON = new BooleanSetting("revanced_hide_download_button", FALSE);
    public static final BooleanSetting HIDE_THANKS_BUTTON = new BooleanSetting("revanced_hide_thanks_button", TRUE);
    public static final BooleanSetting HIDE_CLIP_BUTTON = new BooleanSetting("revanced_hide_clip_button", TRUE);
    public static final BooleanSetting HIDE_PLAYLIST_BUTTON = new BooleanSetting("revanced_hide_playlist_button", FALSE);

    // Player flyout menu items
    public static final BooleanSetting HIDE_CAPTIONS_MENU = new BooleanSetting("revanced_hide_player_flyout_captions", FALSE);
    public static final BooleanSetting HIDE_ADDITIONAL_SETTINGS_MENU = new BooleanSetting("revanced_hide_player_flyout_additional_settings", FALSE);
    public static final BooleanSetting HIDE_LOOP_VIDEO_MENU = new BooleanSetting("revanced_hide_player_flyout_loop_video", FALSE);
    public static final BooleanSetting HIDE_AMBIENT_MODE_MENU = new BooleanSetting("revanced_hide_player_flyout_ambient_mode", FALSE);
    public static final BooleanSetting HIDE_HELP_MENU = new BooleanSetting("revanced_hide_player_flyout_help", TRUE);
    public static final BooleanSetting HIDE_SPEED_MENU = new BooleanSetting("revanced_hide_player_flyout_speed", FALSE);
    public static final BooleanSetting HIDE_MORE_INFO_MENU = new BooleanSetting("revanced_hide_player_flyout_more_info", TRUE);
    public static final BooleanSetting HIDE_LOCK_SCREEN_MENU = new BooleanSetting("revanced_hide_player_flyout_lock_screen", FALSE);
    public static final BooleanSetting HIDE_AUDIO_TRACK_MENU = new BooleanSetting("revanced_hide_player_flyout_audio_track", FALSE);
    public static final BooleanSetting HIDE_WATCH_IN_VR_MENU = new BooleanSetting("revanced_hide_player_flyout_watch_in_vr", TRUE);
    public static final BooleanSetting HIDE_VIDEO_QUALITY_MENU_FOOTER = new BooleanSetting("revanced_hide_video_quality_menu_footer", FALSE);

    // General layout
    public static final StringSetting START_PAGE = new StringSetting("revanced_start_page", "");
    public static final BooleanSetting SPOOF_APP_VERSION = new BooleanSetting("revanced_spoof_app_version", FALSE, true, "revanced_spoof_app_version_user_dialog_message");
    public static final StringSetting SPOOF_APP_VERSION_TARGET = new StringSetting("revanced_spoof_app_version_target", "17.33.42", true, parent(SPOOF_APP_VERSION));
    public static final BooleanSetting TABLET_LAYOUT = new BooleanSetting("revanced_tablet_layout", FALSE, true, "revanced_tablet_layout_user_dialog_message");
    public static final BooleanSetting WIDE_SEARCHBAR = new BooleanSetting("revanced_wide_searchbar", FALSE, true);
    public static final BooleanSetting BYPASS_IMAGE_REGION_RESTRICTIONS = new BooleanSetting("revanced_bypass_image_region_restrictions", FALSE, true);
    public static final BooleanSetting REMOVE_VIEWER_DISCRETION_DIALOG = new BooleanSetting("revanced_remove_viewer_discretion_dialog", FALSE,
            "revanced_remove_viewer_discretion_dialog_user_dialog_message");

    // Custom filter
    public static final BooleanSetting CUSTOM_FILTER = new BooleanSetting("revanced_custom_filter", FALSE);
    public static final StringSetting CUSTOM_FILTER_STRINGS = new StringSetting("revanced_custom_filter_strings", "", true, parent(CUSTOM_FILTER));

    // Navigation buttons
    public static final BooleanSetting HIDE_HOME_BUTTON = new BooleanSetting("revanced_hide_home_button", FALSE, true);
    public static final BooleanSetting HIDE_CREATE_BUTTON = new BooleanSetting("revanced_hide_create_button", TRUE, true);
    public static final BooleanSetting HIDE_SHORTS_BUTTON = new BooleanSetting("revanced_hide_shorts_button", TRUE, true);
    public static final BooleanSetting HIDE_SUBSCRIPTIONS_BUTTON = new BooleanSetting("revanced_hide_subscriptions_button", FALSE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BUTTON_LABELS = new BooleanSetting("revanced_hide_navigation_button_labels", FALSE, true);
    public static final BooleanSetting SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON = new BooleanSetting("revanced_switch_create_with_notifications_button", TRUE, true);

    // Shorts
    public static final BooleanSetting DISABLE_RESUMING_SHORTS_PLAYER = new BooleanSetting("revanced_disable_resuming_shorts_player", FALSE);
    public static final BooleanSetting HIDE_SHORTS_HOME = new BooleanSetting("revanced_hide_shorts_home", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SUBSCRIPTIONS = new BooleanSetting("revanced_hide_shorts_subscriptions", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SEARCH = new BooleanSetting("revanced_hide_shorts_search", FALSE);
    public static final BooleanSetting HIDE_SHORTS_JOIN_BUTTON = new BooleanSetting("revanced_hide_shorts_join_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_SUBSCRIBE_BUTTON = new BooleanSetting("revanced_hide_shorts_subscribe_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_PAUSED_OVERLAY_BUTTONS = new BooleanSetting("revanced_hide_shorts_paused_overlay_buttons", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SHOP_BUTTON = new BooleanSetting("revanced_hide_shorts_shop_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_TAGGED_PRODUCTS = new BooleanSetting("revanced_hide_shorts_tagged_products", TRUE);
    public static final BooleanSetting HIDE_SHORTS_LOCATION_LABEL = new BooleanSetting("revanced_hide_shorts_location_label", FALSE);
    // Save sound to playlist and Search suggestions may have been A/B tests that were abandoned by YT, and it's not clear if these are still used.
    public static final BooleanSetting HIDE_SHORTS_SAVE_SOUND_BUTTON = new BooleanSetting("revanced_hide_shorts_save_sound_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SEARCH_SUGGESTIONS = new BooleanSetting("revanced_hide_shorts_search_suggestions", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SUPER_THANKS_BUTTON = new BooleanSetting("revanced_hide_shorts_super_thanks_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_LIKE_BUTTON = new BooleanSetting("revanced_hide_shorts_like_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_DISLIKE_BUTTON = new BooleanSetting("revanced_hide_shorts_dislike_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_COMMENTS_BUTTON = new BooleanSetting("revanced_hide_shorts_comments_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_REMIX_BUTTON = new BooleanSetting("revanced_hide_shorts_remix_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_SHARE_BUTTON = new BooleanSetting("revanced_hide_shorts_share_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_INFO_PANEL = new BooleanSetting("revanced_hide_shorts_info_panel", TRUE);
    public static final BooleanSetting HIDE_SHORTS_SOUND_BUTTON = new BooleanSetting("revanced_hide_shorts_sound_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_CHANNEL_BAR = new BooleanSetting("revanced_hide_shorts_channel_bar", FALSE);
    public static final BooleanSetting HIDE_SHORTS_VIDEO_TITLE = new BooleanSetting("revanced_hide_shorts_video_title", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SOUND_METADATA_LABEL = new BooleanSetting("revanced_hide_shorts_sound_metadata_label", FALSE);
    public static final BooleanSetting HIDE_SHORTS_FULL_VIDEO_LINK_LABEL = new BooleanSetting("revanced_hide_shorts_full_video_link_label", FALSE);
    public static final BooleanSetting HIDE_SHORTS_NAVIGATION_BAR = new BooleanSetting("revanced_hide_shorts_navigation_bar", TRUE, true);

    // Seekbar
    public static final BooleanSetting DISABLE_PRECISE_SEEKING_GESTURE = new BooleanSetting("revanced_disable_precise_seeking_gesture", TRUE);
    public static final BooleanSetting SEEKBAR_TAPPING = new BooleanSetting("revanced_seekbar_tapping", TRUE);
    public static final BooleanSetting SLIDE_TO_SEEK = new BooleanSetting("revanced_slide_to_seek", FALSE);
    public static final BooleanSetting RESTORE_OLD_SEEKBAR_THUMBNAILS = new BooleanSetting("revanced_restore_old_seekbar_thumbnails", TRUE);
    public static final BooleanSetting HIDE_SEEKBAR = new BooleanSetting("revanced_hide_seekbar", FALSE, true);
    public static final BooleanSetting HIDE_SEEKBAR_THUMBNAIL = new BooleanSetting("revanced_hide_seekbar_thumbnail", FALSE);
    public static final BooleanSetting SEEKBAR_CUSTOM_COLOR = new BooleanSetting("revanced_seekbar_custom_color", FALSE, true);
    public static final StringSetting SEEKBAR_CUSTOM_COLOR_VALUE = new StringSetting("revanced_seekbar_custom_color_value", "#FF0000", true, parent(SEEKBAR_CUSTOM_COLOR));

    // Misc
    public static final BooleanSetting AUTO_CAPTIONS = new BooleanSetting("revanced_auto_captions", FALSE);
    public static final BooleanSetting DISABLE_ZOOM_HAPTICS = new BooleanSetting("revanced_disable_zoom_haptics", TRUE);
    public static final BooleanSetting EXTERNAL_BROWSER = new BooleanSetting("revanced_external_browser", TRUE, true);
    public static final BooleanSetting AUTO_REPEAT = new BooleanSetting("revanced_auto_repeat", FALSE);
    public static final BooleanSetting SPOOF_DEVICE_DIMENSIONS = new BooleanSetting("revanced_spoof_device_dimensions", FALSE, true,
            "revanced_spoof_device_dimensions_user_dialog_message");
    public static final BooleanSetting BYPASS_URL_REDIRECTS = new BooleanSetting("revanced_bypass_url_redirects", TRUE);
    public static final BooleanSetting ANNOUNCEMENTS = new BooleanSetting("revanced_announcements", TRUE);
    public static final BooleanSetting SPOOF_CLIENT = new BooleanSetting("revanced_spoof_client", TRUE, true,"revanced_spoof_client_user_dialog_message");
    public static final BooleanSetting SPOOF_CLIENT_IOS_FORCE_AVC = new BooleanSetting("revanced_spoof_client_ios_force_avc", FALSE, true,
            "revanced_spoof_client_ios_force_avc_user_dialog_message", new SpoofClientPatch.ForceiOSAVCAvailability());
    public static final EnumSetting<ClientType> SPOOF_CLIENT_TYPE = new EnumSetting<>("revanced_spoof_client_type", ClientType.IOS, true, parent(SPOOF_CLIENT));
    @Deprecated
    public static final StringSetting DEPRECATED_ANNOUNCEMENT_LAST_HASH = new StringSetting("revanced_announcement_last_hash", "");
    public static final IntegerSetting ANNOUNCEMENT_LAST_ID = new IntegerSetting("revanced_announcement_last_id", -1);
    public static final BooleanSetting CHECK_WATCH_HISTORY_DOMAIN_NAME = new BooleanSetting("revanced_check_watch_history_domain_name", TRUE, false, false);
    public static final BooleanSetting REMOVE_TRACKING_QUERY_PARAMETER = new BooleanSetting("revanced_remove_tracking_query_parameter", TRUE);

    // Debugging
    /**
     * When enabled, share the debug logs with care.
     * The buffer contains select user data, including the client ip address and information that could identify the end user.
     */
    public static final BooleanSetting DEBUG_PROTOBUFFER = new BooleanSetting("revanced_debug_protobuffer", FALSE, parent(BaseSettings.DEBUG));

    // Old deprecated signature spoofing
    @Deprecated public static final BooleanSetting SPOOF_SIGNATURE = new BooleanSetting("revanced_spoof_signature_verification_enabled", TRUE, true, false,
            "revanced_spoof_signature_verification_enabled_user_dialog_message", null);
    @Deprecated public static final BooleanSetting SPOOF_SIGNATURE_IN_FEED = new BooleanSetting("revanced_spoof_signature_in_feed_enabled", FALSE, false, false, null,
            parent(SPOOF_SIGNATURE));
    @Deprecated public static final BooleanSetting SPOOF_STORYBOARD_RENDERER = new BooleanSetting("revanced_spoof_storyboard", TRUE, true, false, null,
            parent(SPOOF_SIGNATURE));

    // Swipe controls
    public static final BooleanSetting SWIPE_BRIGHTNESS = new BooleanSetting("revanced_swipe_brightness", TRUE);
    public static final BooleanSetting SWIPE_VOLUME = new BooleanSetting("revanced_swipe_volume", TRUE);
    public static final BooleanSetting SWIPE_PRESS_TO_ENGAGE = new BooleanSetting("revanced_swipe_press_to_engage", FALSE, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final BooleanSetting SWIPE_HAPTIC_FEEDBACK = new BooleanSetting("revanced_swipe_haptic_feedback", TRUE, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final IntegerSetting SWIPE_MAGNITUDE_THRESHOLD = new IntegerSetting("revanced_swipe_threshold", 30, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final IntegerSetting SWIPE_OVERLAY_BACKGROUND_ALPHA = new IntegerSetting("revanced_swipe_overlay_background_alpha", 127, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final IntegerSetting SWIPE_OVERLAY_TEXT_SIZE = new IntegerSetting("revanced_swipe_text_overlay_size", 22, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final LongSetting SWIPE_OVERLAY_TIMEOUT = new LongSetting("revanced_swipe_overlay_timeout", 500L, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final BooleanSetting SWIPE_SAVE_AND_RESTORE_BRIGHTNESS = new BooleanSetting("revanced_swipe_save_and_restore_brightness", TRUE, true, parent(SWIPE_BRIGHTNESS));
    public static final FloatSetting SWIPE_BRIGHTNESS_VALUE = new FloatSetting("revanced_swipe_brightness_value", -1f);
    public static final BooleanSetting SWIPE_LOWEST_VALUE_ENABLE_AUTO_BRIGHTNESS = new BooleanSetting("revanced_swipe_lowest_value_enable_auto_brightness", FALSE, true, parent(SWIPE_BRIGHTNESS));

    // ReturnYoutubeDislike
    public static final BooleanSetting RYD_ENABLED = new BooleanSetting("ryd_enabled", TRUE);
    public static final StringSetting RYD_USER_ID = new StringSetting("ryd_user_id", "", false, false);
    public static final BooleanSetting RYD_SHORTS = new BooleanSetting("ryd_shorts", TRUE, parent(RYD_ENABLED));
    public static final BooleanSetting RYD_DISLIKE_PERCENTAGE = new BooleanSetting("ryd_dislike_percentage", FALSE, parent(RYD_ENABLED));
    public static final BooleanSetting RYD_COMPACT_LAYOUT = new BooleanSetting("ryd_compact_layout", FALSE, parent(RYD_ENABLED));
    public static final BooleanSetting RYD_TOAST_ON_CONNECTION_ERROR = new BooleanSetting("ryd_toast_on_connection_error", TRUE, parent(RYD_ENABLED));

    // SponsorBlock
    public static final BooleanSetting SB_ENABLED = new BooleanSetting("sb_enabled", TRUE);
    /**
     * Do not use directly, instead use {@link SponsorBlockSettings}
     */
    public static final StringSetting SB_PRIVATE_USER_ID = new StringSetting("sb_private_user_id_Do_Not_Share", "");
    @Deprecated
    public static final StringSetting DEPRECATED_SB_UUID_OLD_MIGRATION_SETTING = new StringSetting("uuid", ""); // Delete sometime in 2024
    public static final IntegerSetting SB_CREATE_NEW_SEGMENT_STEP = new IntegerSetting("sb_create_new_segment_step", 150, parent(SB_ENABLED));
    public static final BooleanSetting SB_VOTING_BUTTON = new BooleanSetting("sb_voting_button", FALSE, parent(SB_ENABLED));
    public static final BooleanSetting SB_CREATE_NEW_SEGMENT = new BooleanSetting("sb_create_new_segment", FALSE, parent(SB_ENABLED));
    public static final BooleanSetting SB_COMPACT_SKIP_BUTTON = new BooleanSetting("sb_compact_skip_button", FALSE, parent(SB_ENABLED));
    public static final BooleanSetting SB_AUTO_HIDE_SKIP_BUTTON = new BooleanSetting("sb_auto_hide_skip_button", TRUE, parent(SB_ENABLED));
    public static final BooleanSetting SB_TOAST_ON_SKIP = new BooleanSetting("sb_toast_on_skip", TRUE, parent(SB_ENABLED));
    public static final BooleanSetting SB_TOAST_ON_CONNECTION_ERROR = new BooleanSetting("sb_toast_on_connection_error", TRUE, parent(SB_ENABLED));
    public static final BooleanSetting SB_TRACK_SKIP_COUNT = new BooleanSetting("sb_track_skip_count", TRUE, parent(SB_ENABLED));
    public static final FloatSetting SB_SEGMENT_MIN_DURATION = new FloatSetting("sb_min_segment_duration", 0F, parent(SB_ENABLED));
    public static final BooleanSetting SB_VIDEO_LENGTH_WITHOUT_SEGMENTS = new BooleanSetting("sb_video_length_without_segments", TRUE, parent(SB_ENABLED));
    public static final StringSetting SB_API_URL = new StringSetting("sb_api_url","https://sponsor.ajay.app");
    public static final BooleanSetting SB_USER_IS_VIP = new BooleanSetting("sb_user_is_vip", FALSE);
    public static final IntegerSetting SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS = new IntegerSetting("sb_local_time_saved_number_segments", 0);
    public static final LongSetting SB_LOCAL_TIME_SAVED_MILLISECONDS = new LongSetting("sb_local_time_saved_milliseconds", 0L);
    public static final LongSetting SB_LAST_VIP_CHECK = new LongSetting("sb_last_vip_check", 0L, false, false);
    public static final BooleanSetting SB_HIDE_EXPORT_WARNING = new BooleanSetting("sb_hide_export_warning", FALSE, false, false);
    public static final BooleanSetting SB_SEEN_GUIDELINES = new BooleanSetting("sb_seen_guidelines", FALSE, false, false);

    public static final StringSetting SB_CATEGORY_SPONSOR = new StringSetting("sb_sponsor", SKIP_AUTOMATICALLY_ONCE.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_SPONSOR_COLOR = new StringSetting("sb_sponsor_color","#00D400");
    public static final StringSetting SB_CATEGORY_SELF_PROMO = new StringSetting("sb_selfpromo", MANUAL_SKIP.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_SELF_PROMO_COLOR = new StringSetting("sb_selfpromo_color","#FFFF00");
    public static final StringSetting SB_CATEGORY_INTERACTION = new StringSetting("sb_interaction", MANUAL_SKIP.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_INTERACTION_COLOR = new StringSetting("sb_interaction_color","#CC00FF");
    public static final StringSetting SB_CATEGORY_HIGHLIGHT = new StringSetting("sb_highlight", MANUAL_SKIP.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_HIGHLIGHT_COLOR = new StringSetting("sb_highlight_color","#FF1684");
    public static final StringSetting SB_CATEGORY_INTRO = new StringSetting("sb_intro", MANUAL_SKIP.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_INTRO_COLOR = new StringSetting("sb_intro_color","#00FFFF");
    public static final StringSetting SB_CATEGORY_OUTRO = new StringSetting("sb_outro", MANUAL_SKIP.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_OUTRO_COLOR = new StringSetting("sb_outro_color","#0202ED");
    public static final StringSetting SB_CATEGORY_PREVIEW = new StringSetting("sb_preview", IGNORE.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_PREVIEW_COLOR = new StringSetting("sb_preview_color","#008FD6");
    public static final StringSetting SB_CATEGORY_FILLER = new StringSetting("sb_filler", IGNORE.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_FILLER_COLOR = new StringSetting("sb_filler_color","#7300FF");
    public static final StringSetting SB_CATEGORY_MUSIC_OFFTOPIC = new StringSetting("sb_music_offtopic", MANUAL_SKIP.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_MUSIC_OFFTOPIC_COLOR = new StringSetting("sb_music_offtopic_color","#FF9900");
    public static final StringSetting SB_CATEGORY_UNSUBMITTED = new StringSetting("sb_unsubmitted", SKIP_AUTOMATICALLY.reVancedKeyValue);
    public static final StringSetting SB_CATEGORY_UNSUBMITTED_COLOR = new StringSetting("sb_unsubmitted_color","#FFFFFF");

    static {
        // region Migration

        // Migrate settings from old Preference categories into replacement "revanced_prefs" category.
        // This region must run before all other migration code.

        // The YT and RYD migration portion of this can be removed anytime,
        // but the SB migration should remain until late 2024 or early 2025
        // because it migrates the SB private user id which cannot be recovered if lost.

        // Categories were previously saved without a 'sb_' key prefix, so they need an additional adjustment.
        Set<Setting<?>> sbCategories = new HashSet<>(Arrays.asList(
                SB_CATEGORY_SPONSOR,
                SB_CATEGORY_SPONSOR_COLOR,
                SB_CATEGORY_SELF_PROMO,
                SB_CATEGORY_SELF_PROMO_COLOR,
                SB_CATEGORY_INTERACTION,
                SB_CATEGORY_INTERACTION_COLOR,
                SB_CATEGORY_HIGHLIGHT,
                SB_CATEGORY_HIGHLIGHT_COLOR,
                SB_CATEGORY_INTRO,
                SB_CATEGORY_INTRO_COLOR,
                SB_CATEGORY_OUTRO,
                SB_CATEGORY_OUTRO_COLOR,
                SB_CATEGORY_PREVIEW,
                SB_CATEGORY_PREVIEW_COLOR,
                SB_CATEGORY_FILLER,
                SB_CATEGORY_FILLER_COLOR,
                SB_CATEGORY_MUSIC_OFFTOPIC,
                SB_CATEGORY_MUSIC_OFFTOPIC_COLOR,
                SB_CATEGORY_UNSUBMITTED,
                SB_CATEGORY_UNSUBMITTED_COLOR));

        SharedPrefCategory ytPrefs = new SharedPrefCategory("youtube");
        SharedPrefCategory rydPrefs = new SharedPrefCategory("ryd");
        SharedPrefCategory sbPrefs = new SharedPrefCategory("sponsor-block");
        for (Setting<?> setting : Setting.allLoadedSettings()) {
            String key = setting.key;
            if (setting.key.startsWith("sb_")) {
                if (sbCategories.contains(setting)) {
                    key = key.substring(3); // Remove the "sb_" prefix, as old categories are saved without it.
                }
                migrateFromOldPreferences(sbPrefs, setting, key);
            } else if (setting.key.startsWith("ryd_")) {
                migrateFromOldPreferences(rydPrefs, setting, key);
            } else {
                migrateFromOldPreferences(ytPrefs, setting, key);
            }
        }


        // Do _not_ delete this SB private user id migration property until sometime in 2024.
        // This is the only setting that cannot be reconfigured if lost,
        // and more time should be given for users who rarely upgrade.
        migrateOldSettingToNew(DEPRECATED_SB_UUID_OLD_MIGRATION_SETTING, SB_PRIVATE_USER_ID);


        // Old spoof versions that no longer work reliably.
        if (SpoofAppVersionPatch.isSpoofingToLessThan("17.33.00")) {
            Logger.printInfo(() -> "Resetting spoof app version target");
            Settings.SPOOF_APP_VERSION_TARGET.resetToDefault();
        }


        // Remove any previously saved announcement consumer (a random generated string).
        Setting.preferences.removeKey("revanced_announcement_consumer");

        migrateOldSettingToNew(HIDE_LOAD_MORE_BUTTON, HIDE_SHOW_MORE_BUTTON);

        // endregion
    }
}
