package app.revanced.extension.youtube.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.extension.shared.settings.Setting.parent;
import static app.revanced.extension.shared.settings.Setting.parentNot;
import static app.revanced.extension.shared.settings.Setting.parentsAll;
import static app.revanced.extension.shared.settings.Setting.parentsAny;
import static app.revanced.extension.youtube.patches.ChangeFormFactorPatch.FormFactor;
import static app.revanced.extension.youtube.patches.ChangeHeaderPatch.HeaderLogo;
import static app.revanced.extension.youtube.patches.ChangeStartPagePatch.ChangeStartPageTypeAvailability;
import static app.revanced.extension.youtube.patches.ChangeStartPagePatch.StartPage;
import static app.revanced.extension.youtube.patches.ExitFullscreenPatch.FullscreenMode;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerAnyModernAvailability;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerHideOverlayButtonsAvailability;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerHideRewindOrOverlayOpacityAvailability;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerHideSubtextsAvailability;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerHorizontalDragAvailability;
import static app.revanced.extension.youtube.patches.MiniplayerPatch.MiniplayerType;
import static app.revanced.extension.youtube.patches.OpenShortsInRegularPlayerPatch.ShortsPlayerType;
import static app.revanced.extension.youtube.patches.SeekbarThumbnailsPatch.SeekbarThumbnailsHighQualityAvailability;
import static app.revanced.extension.youtube.patches.components.PlayerFlyoutMenuItemsFilter.HideAudioFlyoutMenuAvailability;
import static app.revanced.extension.youtube.patches.spoof.SpoofVideoStreamsPatch.SpoofClientAv1Availability;
import static app.revanced.extension.youtube.patches.theme.ThemePatch.SplashScreenAnimationStyle;
import static app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController.SponsorBlockDuration;
import static app.revanced.extension.youtube.sponsorblock.objects.CategoryBehaviour.IGNORE;
import static app.revanced.extension.youtube.sponsorblock.objects.CategoryBehaviour.MANUAL_SKIP;
import static app.revanced.extension.youtube.sponsorblock.objects.CategoryBehaviour.SKIP_AUTOMATICALLY;
import static app.revanced.extension.youtube.sponsorblock.objects.CategoryBehaviour.SKIP_AUTOMATICALLY_ONCE;

import android.graphics.Color;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.EnumSetting;
import app.revanced.extension.shared.settings.FloatSetting;
import app.revanced.extension.shared.settings.IntegerSetting;
import app.revanced.extension.shared.settings.LongSetting;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.DeArrowAvailability;
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.StillImagesAvailability;
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.ThumbnailOption;
import app.revanced.extension.youtube.patches.AlternativeThumbnailsPatch.ThumbnailStillTime;
import app.revanced.extension.youtube.patches.MiniplayerPatch;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings;
import app.revanced.extension.youtube.swipecontrols.SwipeControlsConfigurationProvider.SwipeOverlayStyle;

public class Settings extends BaseSettings {
    // Video
    public static final BooleanSetting ADVANCED_VIDEO_QUALITY_MENU = new BooleanSetting("revanced_advanced_video_quality_menu", TRUE);
    public static final BooleanSetting DISABLE_HDR_VIDEO = new BooleanSetting("revanced_disable_hdr_video", FALSE);
    public static final BooleanSetting FORCE_AVC_CODEC = new BooleanSetting("revanced_force_avc_codec", FALSE, true, "revanced_force_avc_codec_user_dialog_message");
    public static final BooleanSetting FORCE_ORIGINAL_AUDIO = new BooleanSetting("revanced_force_original_audio", TRUE, true);
    public static final IntegerSetting VIDEO_QUALITY_DEFAULT_WIFI = new IntegerSetting("revanced_video_quality_default_wifi", -2);
    public static final IntegerSetting VIDEO_QUALITY_DEFAULT_MOBILE = new IntegerSetting("revanced_video_quality_default_mobile", -2);
    public static final BooleanSetting REMEMBER_VIDEO_QUALITY_LAST_SELECTED = new BooleanSetting("revanced_remember_video_quality_last_selected", FALSE);
    public static final IntegerSetting SHORTS_QUALITY_DEFAULT_WIFI = new IntegerSetting("revanced_shorts_quality_default_wifi", -2, true);
    public static final IntegerSetting SHORTS_QUALITY_DEFAULT_MOBILE = new IntegerSetting("revanced_shorts_quality_default_mobile", -2, true);
    public static final BooleanSetting REMEMBER_SHORTS_QUALITY_LAST_SELECTED = new BooleanSetting("revanced_remember_shorts_quality_last_selected", FALSE);
    public static final BooleanSetting REMEMBER_VIDEO_QUALITY_LAST_SELECTED_TOAST = new BooleanSetting("revanced_remember_video_quality_last_selected_toast", TRUE, false,
            parentsAny(REMEMBER_VIDEO_QUALITY_LAST_SELECTED, REMEMBER_SHORTS_QUALITY_LAST_SELECTED));

    // Speed
    public static final FloatSetting SPEED_TAP_AND_HOLD = new FloatSetting("revanced_speed_tap_and_hold", 2.0f, true);
    public static final BooleanSetting REMEMBER_PLAYBACK_SPEED_LAST_SELECTED = new BooleanSetting("revanced_remember_playback_speed_last_selected", FALSE);
    public static final BooleanSetting REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_TOAST = new BooleanSetting("revanced_remember_playback_speed_last_selected_toast", TRUE, false,
            parent(REMEMBER_PLAYBACK_SPEED_LAST_SELECTED));
    public static final FloatSetting PLAYBACK_SPEED_DEFAULT = new FloatSetting("revanced_playback_speed_default", -2.0f);
    public static final BooleanSetting CUSTOM_SPEED_MENU = new BooleanSetting("revanced_custom_speed_menu", TRUE);
    public static final BooleanSetting RESTORE_OLD_SPEED_MENU = new BooleanSetting("revanced_restore_old_speed_menu", FALSE, parent(CUSTOM_SPEED_MENU));
    public static final StringSetting CUSTOM_PLAYBACK_SPEEDS = new StringSetting("revanced_custom_playback_speeds",
            "0.25\n0.5\n0.75\n1.0\n1.25\n1.5\n1.75\n2.0\n2.5\n3.0\n4.0\n5.0\n6.0\n7.0\n8.0", true);

    // Ads
    public static final BooleanSetting HIDE_CREATOR_STORE_SHELF = new BooleanSetting("revanced_hide_creator_store_shelf", TRUE);
    public static final BooleanSetting HIDE_END_SCREEN_STORE_BANNER = new BooleanSetting("revanced_hide_end_screen_store_banner", TRUE, true);
    public static final BooleanSetting HIDE_FULLSCREEN_ADS = new BooleanSetting("revanced_hide_fullscreen_ads", TRUE);
    public static final BooleanSetting HIDE_GENERAL_ADS = new BooleanSetting("revanced_hide_general_ads", TRUE);
    public static final BooleanSetting HIDE_GET_PREMIUM = new BooleanSetting("revanced_hide_get_premium", TRUE);
    public static final BooleanSetting HIDE_LATEST_POSTS = new BooleanSetting("revanced_hide_latest_posts", TRUE);
    public static final BooleanSetting HIDE_MERCHANDISE_BANNERS = new BooleanSetting("revanced_hide_merchandise_banners", TRUE);
    public static final BooleanSetting HIDE_PAID_PROMOTION_LABEL = new BooleanSetting("revanced_hide_paid_promotion_label", TRUE);
    public static final BooleanSetting HIDE_SELF_SPONSOR = new BooleanSetting("revanced_hide_self_sponsor_ads", TRUE);
    public static final BooleanSetting HIDE_SHOPPING_LINKS = new BooleanSetting("revanced_hide_shopping_links", TRUE);
    public static final BooleanSetting HIDE_VIDEO_ADS = new BooleanSetting("revanced_hide_video_ads", TRUE, true);
    public static final BooleanSetting HIDE_VIEW_PRODUCTS_BANNER = new BooleanSetting("revanced_hide_view_products_banner", TRUE);
    public static final BooleanSetting HIDE_WEB_SEARCH_RESULTS = new BooleanSetting("revanced_hide_web_search_results", TRUE);

    // Feed
    public static final BooleanSetting HIDE_ALBUM_CARDS = new BooleanSetting("revanced_hide_album_cards", FALSE, true);
    public static final BooleanSetting HIDE_ARTIST_CARDS = new BooleanSetting("revanced_hide_artist_cards", FALSE);
    public static final BooleanSetting HIDE_CHIPS_SHELF = new BooleanSetting("revanced_hide_chips_shelf", TRUE);
    public static final BooleanSetting HIDE_COMMUNITY_POSTS = new BooleanSetting("revanced_hide_community_posts", FALSE);
    public static final BooleanSetting HIDE_COMPACT_BANNER = new BooleanSetting("revanced_hide_compact_banner", TRUE);
    public static final BooleanSetting HIDE_CROWDFUNDING_BOX = new BooleanSetting("revanced_hide_crowdfunding_box", FALSE, true);
    public static final BooleanSetting HIDE_DOODLES = new BooleanSetting("revanced_hide_doodles", FALSE, true, "revanced_hide_doodles_user_dialog_message");
    public static final BooleanSetting HIDE_EXPANDABLE_CARD = new BooleanSetting("revanced_hide_expandable_card", TRUE);
    public static final BooleanSetting HIDE_FILTER_BAR_FEED_IN_FEED = new BooleanSetting("revanced_hide_filter_bar_feed_in_feed", FALSE, true);
    public static final BooleanSetting HIDE_FILTER_BAR_FEED_IN_HISTORY = new BooleanSetting("revanced_hide_filter_bar_feed_in_history", FALSE);
    public static final BooleanSetting HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS = new BooleanSetting("revanced_hide_filter_bar_feed_in_related_videos", FALSE, true);
    public static final BooleanSetting HIDE_FILTER_BAR_FEED_IN_SEARCH = new BooleanSetting("revanced_hide_filter_bar_feed_in_search", FALSE, true);
    public static final BooleanSetting HIDE_FLOATING_MICROPHONE_BUTTON = new BooleanSetting("revanced_hide_floating_microphone_button", TRUE, true);
    public static final BooleanSetting HIDE_HORIZONTAL_SHELVES = new BooleanSetting("revanced_hide_horizontal_shelves", TRUE);
    public static final BooleanSetting HIDE_IMAGE_SHELF = new BooleanSetting("revanced_hide_image_shelf", TRUE);
    public static final BooleanSetting HIDE_MIX_PLAYLISTS = new BooleanSetting("revanced_hide_mix_playlists", TRUE);
    public static final BooleanSetting HIDE_MOVIES_SECTION = new BooleanSetting("revanced_hide_movies_section", TRUE);
    public static final BooleanSetting HIDE_NOTIFY_ME_BUTTON = new BooleanSetting("revanced_hide_notify_me_button", TRUE);
    public static final BooleanSetting HIDE_PLAYABLES = new BooleanSetting("revanced_hide_playables", TRUE);
    public static final BooleanSetting HIDE_SHOW_MORE_BUTTON = new BooleanSetting("revanced_hide_show_more_button", TRUE, true);
    public static final BooleanSetting HIDE_SURVEYS = new BooleanSetting("revanced_hide_surveys", TRUE);
    public static final BooleanSetting HIDE_TICKET_SHELF = new BooleanSetting("revanced_hide_ticket_shelf", FALSE);
    public static final BooleanSetting HIDE_UPLOAD_TIME = new BooleanSetting("revanced_hide_upload_time", FALSE, "revanced_hide_upload_time_user_dialog_message");
    public static final BooleanSetting HIDE_VIDEO_RECOMMENDATION_LABELS = new BooleanSetting("revanced_hide_video_recommendation_labels", TRUE);
    public static final BooleanSetting HIDE_VIEW_COUNT = new BooleanSetting("revanced_hide_view_count", FALSE, "revanced_hide_view_count_user_dialog_message");
    public static final BooleanSetting HIDE_VISUAL_SPACER = new BooleanSetting("revanced_hide_visual_spacer", TRUE);

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

    // Channel page
    public static final BooleanSetting HIDE_COMMUNITY_BUTTON = new BooleanSetting("revanced_hide_community_button", TRUE);
    public static final BooleanSetting HIDE_FOR_YOU_SHELF = new BooleanSetting("revanced_hide_for_you_shelf", FALSE);
    public static final BooleanSetting HIDE_JOIN_BUTTON = new BooleanSetting("revanced_hide_join_button", FALSE);
    public static final BooleanSetting HIDE_LINKS_PREVIEW = new BooleanSetting("revanced_hide_links_preview", TRUE);
    public static final BooleanSetting HIDE_MEMBERS_SHELF = new BooleanSetting("revanced_hide_members_shelf", TRUE);
    public static final BooleanSetting HIDE_STORE_BUTTON = new BooleanSetting("revanced_hide_store_button", TRUE);
    public static final BooleanSetting HIDE_SUBSCRIBE_BUTTON_IN_CHANNEL_PAGE = new BooleanSetting("revanced_hide_subscribe_button_in_channel_page", FALSE);

    // Player
    public static final BooleanSetting COPY_VIDEO_URL = new BooleanSetting("revanced_copy_video_url", FALSE);
    public static final BooleanSetting COPY_VIDEO_URL_TIMESTAMP = new BooleanSetting("revanced_copy_video_url_timestamp", TRUE);
    public static final BooleanSetting DISABLE_AUTO_CAPTIONS = new BooleanSetting("revanced_disable_auto_captions", FALSE, true);
    public static final BooleanSetting DISABLE_CHAPTER_SKIP_DOUBLE_TAP = new BooleanSetting("revanced_disable_chapter_skip_double_tap", FALSE);
    public static final BooleanSetting DISABLE_FULLSCREEN_AMBIENT_MODE = new BooleanSetting("revanced_disable_fullscreen_ambient_mode", TRUE, true);
    public static final BooleanSetting DISABLE_ROLLING_NUMBER_ANIMATIONS = new BooleanSetting("revanced_disable_rolling_number_animations", FALSE);
    public static final EnumSetting<FullscreenMode> EXIT_FULLSCREEN = new EnumSetting<>("revanced_exit_fullscreen", FullscreenMode.DISABLED);
    public static final BooleanSetting HIDE_AUTOPLAY_BUTTON = new BooleanSetting("revanced_hide_autoplay_button", TRUE, true);
    public static final BooleanSetting HIDE_CAPTIONS_BUTTON = new BooleanSetting("revanced_hide_captions_button", FALSE);
    public static final BooleanSetting HIDE_CAST_BUTTON = new BooleanSetting("revanced_hide_cast_button", TRUE, true);
    public static final BooleanSetting HIDE_CHANNEL_BAR = new BooleanSetting("revanced_hide_channel_bar", FALSE);
    public static final BooleanSetting HIDE_EMERGENCY_BOX = new BooleanSetting("revanced_hide_emergency_box", TRUE);
    public static final BooleanSetting HIDE_ENDSCREEN_CARDS = new BooleanSetting("revanced_hide_endscreen_cards", FALSE);
    public static final BooleanSetting HIDE_END_SCREEN_SUGGESTED_VIDEO = new BooleanSetting("revanced_end_screen_suggested_video", FALSE, true);
    public static final BooleanSetting HIDE_INFO_CARDS = new BooleanSetting("revanced_hide_info_cards", FALSE);
    public static final BooleanSetting HIDE_INFO_PANELS = new BooleanSetting("revanced_hide_info_panels", TRUE);
    public static final BooleanSetting HIDE_JOIN_MEMBERSHIP_BUTTON = new BooleanSetting("revanced_hide_join_membership_button", TRUE);
    public static final BooleanSetting HIDE_MEDICAL_PANELS = new BooleanSetting("revanced_hide_medical_panels", TRUE);
    public static final BooleanSetting HIDE_PLAYER_CONTROL_BUTTONS_BACKGROUND = new BooleanSetting("revanced_hide_player_control_buttons_background", FALSE, true);
    public static final BooleanSetting HIDE_PLAYER_PREVIOUS_NEXT_BUTTONS = new BooleanSetting("revanced_hide_player_previous_next_buttons", FALSE, true);
    public static final BooleanSetting HIDE_QUICK_ACTIONS = new BooleanSetting("revanced_hide_quick_actions", FALSE);
    public static final BooleanSetting HIDE_RELATED_VIDEOS_OVERLAY = new BooleanSetting("revanced_hide_related_videos_overlay", FALSE, true);
    public static final BooleanSetting HIDE_RELATED_VIDEOS = new BooleanSetting("revanced_hide_related_videos", FALSE);
    public static final BooleanSetting HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES = new BooleanSetting("revanced_hide_subscribers_community_guidelines", TRUE);
    public static final BooleanSetting HIDE_TIMED_REACTIONS = new BooleanSetting("revanced_hide_timed_reactions", TRUE);
    public static final BooleanSetting HIDE_VIDEO_CHANNEL_WATERMARK = new BooleanSetting("revanced_hide_channel_watermark", TRUE);
    public static final BooleanSetting OPEN_VIDEOS_FULLSCREEN_PORTRAIT = new BooleanSetting("revanced_open_videos_fullscreen_portrait", FALSE);
    public static final BooleanSetting PLAYBACK_SPEED_DIALOG_BUTTON = new BooleanSetting("revanced_playback_speed_dialog_button", FALSE);
    public static final BooleanSetting VIDEO_QUALITY_DIALOG_BUTTON = new BooleanSetting("revanced_video_quality_dialog_button", FALSE);
    public static final IntegerSetting PLAYER_OVERLAY_OPACITY = new IntegerSetting("revanced_player_overlay_opacity", 100, true);
    public static final BooleanSetting PLAYER_POPUP_PANELS = new BooleanSetting("revanced_hide_player_popup_panels", FALSE);

    // Miniplayer
    public static final EnumSetting<MiniplayerType> MINIPLAYER_TYPE = new EnumSetting<>("revanced_miniplayer_type", MiniplayerType.DEFAULT, true);
    public static final BooleanSetting MINIPLAYER_DISABLE_DRAG_AND_DROP = new BooleanSetting("revanced_miniplayer_disable_drag_and_drop", FALSE, true, new MiniplayerAnyModernAvailability());
    public static final BooleanSetting MINIPLAYER_DISABLE_HORIZONTAL_DRAG = new BooleanSetting("revanced_miniplayer_disable_horizontal_drag", FALSE, true, new MiniplayerHorizontalDragAvailability());
    public static final BooleanSetting MINIPLAYER_DISABLE_ROUNDED_CORNERS = new BooleanSetting("revanced_miniplayer_disable_rounded_corners", FALSE, true, new MiniplayerAnyModernAvailability());
    public static final BooleanSetting MINIPLAYER_DOUBLE_TAP_ACTION = new BooleanSetting("revanced_miniplayer_double_tap_action", TRUE, true, new MiniplayerAnyModernAvailability());
    public static final BooleanSetting MINIPLAYER_HIDE_OVERLAY_BUTTONS = new BooleanSetting("revanced_miniplayer_hide_overlay_buttons", FALSE, true, new MiniplayerHideOverlayButtonsAvailability());
    public static final BooleanSetting MINIPLAYER_HIDE_SUBTEXT = new BooleanSetting("revanced_miniplayer_hide_subtext", FALSE, true, new MiniplayerHideSubtextsAvailability());
    public static final BooleanSetting MINIPLAYER_HIDE_REWIND_FORWARD = new BooleanSetting("revanced_miniplayer_hide_rewind_forward", TRUE, true, new MiniplayerPatch.MiniplayerHideRewindOrOverlayOpacityAvailability());
    public static final IntegerSetting MINIPLAYER_WIDTH_DIP = new IntegerSetting("revanced_miniplayer_width_dip", 192, true, new MiniplayerAnyModernAvailability());
    public static final IntegerSetting MINIPLAYER_OPACITY = new IntegerSetting("revanced_miniplayer_opacity", 100, true, new MiniplayerHideRewindOrOverlayOpacityAvailability());

    // External downloader
    public static final BooleanSetting EXTERNAL_DOWNLOADER = new BooleanSetting("revanced_external_downloader", FALSE);
    public static final BooleanSetting EXTERNAL_DOWNLOADER_ACTION_BUTTON = new BooleanSetting("revanced_external_downloader_action_button", FALSE);
    public static final StringSetting EXTERNAL_DOWNLOADER_PACKAGE_NAME = new StringSetting("revanced_external_downloader_name",
            "com.deniscerri.ytdl" /* YTDLnis */, parentsAny(EXTERNAL_DOWNLOADER, EXTERNAL_DOWNLOADER_ACTION_BUTTON));

    // Comments
    public static final BooleanSetting HIDE_COMMENTS_AI_CHAT_SUMMARY = new BooleanSetting("revanced_hide_comments_ai_chat_summary", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_AI_SUMMARY = new BooleanSetting("revanced_hide_comments_ai_summary", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_BY_MEMBERS_HEADER = new BooleanSetting("revanced_hide_comments_by_members_header", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_CHANNEL_GUIDELINES = new BooleanSetting("revanced_hide_comments_channel_guidelines", TRUE);
    public static final BooleanSetting HIDE_COMMENTS_COMMUNITY_GUIDELINES = new BooleanSetting("revanced_hide_comments_community_guidelines", TRUE);
    public static final BooleanSetting HIDE_COMMENTS_CREATE_A_SHORT_BUTTON = new BooleanSetting("revanced_hide_comments_create_a_short_button", TRUE);
    public static final BooleanSetting HIDE_COMMENTS_PREVIEW_COMMENT = new BooleanSetting("revanced_hide_comments_preview_comment", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_EMOJI_AND_TIMESTAMP_BUTTONS = new BooleanSetting("revanced_hide_comments_emoji_and_timestamp_buttons", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_SECTION = new BooleanSetting("revanced_hide_comments_section", FALSE);
    public static final BooleanSetting HIDE_COMMENTS_THANKS_BUTTON = new BooleanSetting("revanced_hide_comments_thanks_button", TRUE);

    // Description
    public static final BooleanSetting HIDE_AI_GENERATED_VIDEO_SUMMARY_SECTION = new BooleanSetting("revanced_hide_ai_generated_video_summary_section", FALSE);
    public static final BooleanSetting HIDE_ASK_SECTION = new BooleanSetting("revanced_hide_ask_section", FALSE);
    public static final BooleanSetting HIDE_ATTRIBUTES_SECTION = new BooleanSetting("revanced_hide_attributes_section", FALSE);
    public static final BooleanSetting HIDE_CHAPTERS_SECTION = new BooleanSetting("revanced_hide_chapters_section", TRUE);
    public static final BooleanSetting HIDE_HOW_THIS_WAS_MADE_SECTION = new BooleanSetting("revanced_hide_how_this_was_made_section", FALSE);
    public static final BooleanSetting HIDE_HYPE_POINTS = new BooleanSetting("revanced_hide_hype_points", FALSE);
    public static final BooleanSetting HIDE_INFO_CARDS_SECTION = new BooleanSetting("revanced_hide_info_cards_section", TRUE);
    public static final BooleanSetting HIDE_FEATURED_LINKS_SECTION = new BooleanSetting("revanced_hide_featured_links_section", FALSE, parentNot(HIDE_INFO_CARDS_SECTION));
    public static final BooleanSetting HIDE_FEATURED_VIDEOS_SECTION = new BooleanSetting("revanced_hide_featured_videos_section", FALSE, parentNot(HIDE_INFO_CARDS_SECTION));
    public static final BooleanSetting HIDE_SUBSCRIBE_BUTTON = new BooleanSetting("revanced_hide_subscribe_button", FALSE, parentNot(HIDE_INFO_CARDS_SECTION));
    public static final BooleanSetting HIDE_KEY_CONCEPTS_SECTION = new BooleanSetting("revanced_hide_key_concepts_section", FALSE);
    public static final BooleanSetting HIDE_PODCAST_SECTION = new BooleanSetting("revanced_hide_podcast_section", TRUE);
    public static final BooleanSetting HIDE_TRANSCRIPT_SECTION = new BooleanSetting("revanced_hide_transcript_section", TRUE);

    // Action buttons
    public static final BooleanSetting DISABLE_LIKE_SUBSCRIBE_GLOW = new BooleanSetting("revanced_disable_like_subscribe_glow", FALSE);
    public static final BooleanSetting HIDE_ASK_BUTTON = new BooleanSetting("revanced_hide_ask_button", FALSE);
    public static final BooleanSetting HIDE_CLIP_BUTTON = new BooleanSetting("revanced_hide_clip_button", TRUE);
    public static final BooleanSetting HIDE_COMMENTS_BUTTON = new BooleanSetting("revanced_hide_comments_button", TRUE);
    public static final BooleanSetting HIDE_DOWNLOAD_BUTTON = new BooleanSetting("revanced_hide_download_button", FALSE);
    public static final BooleanSetting HIDE_HYPE_BUTTON = new BooleanSetting("revanced_hide_hype_button", FALSE);
    public static final BooleanSetting HIDE_LIKE_DISLIKE_BUTTON = new BooleanSetting("revanced_hide_like_dislike_button", FALSE);
    public static final BooleanSetting HIDE_PROMOTE_BUTTON = new BooleanSetting("revanced_hide_promote_button", FALSE);
    public static final BooleanSetting HIDE_REMIX_BUTTON = new BooleanSetting("revanced_hide_remix_button", FALSE);
    public static final BooleanSetting HIDE_REPORT_BUTTON = new BooleanSetting("revanced_hide_report_button", FALSE);
    public static final BooleanSetting HIDE_SAVE_BUTTON = new BooleanSetting("revanced_hide_save_button", FALSE);
    public static final BooleanSetting HIDE_SHARE_BUTTON = new BooleanSetting("revanced_hide_share_button", FALSE);
    public static final BooleanSetting HIDE_SHOP_BUTTON = new BooleanSetting("revanced_hide_shop_button", TRUE);
    public static final BooleanSetting HIDE_STOP_ADS_BUTTON = new BooleanSetting("revanced_hide_stop_ads_button", TRUE);
    public static final BooleanSetting HIDE_THANKS_BUTTON = new BooleanSetting("revanced_hide_thanks_button", TRUE);

    // Player flyout menu items
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_ADDITIONAL_SETTINGS = new BooleanSetting("revanced_hide_player_flyout_additional_settings", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_AMBIENT_MODE = new BooleanSetting("revanced_hide_player_flyout_ambient_mode", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_AUDIO_TRACK = new BooleanSetting("revanced_hide_player_flyout_audio_track", FALSE, new HideAudioFlyoutMenuAvailability());
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_CAPTIONS = new BooleanSetting("revanced_hide_player_flyout_captions", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_HELP = new BooleanSetting("revanced_hide_player_flyout_help", TRUE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_LISTEN_WITH_YOUTUBE_MUSIC = new BooleanSetting("revanced_hide_player_flyout_listen_with_youtube_music", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_LOCK_SCREEN = new BooleanSetting("revanced_hide_player_flyout_lock_screen", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_LOOP_VIDEO = new BooleanSetting("revanced_hide_player_flyout_loop_video", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_SLEEP_TIMER = new BooleanSetting("revanced_hide_player_flyout_sleep_timer", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_SPEED = new BooleanSetting("revanced_hide_player_flyout_speed", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_STABLE_VOLUME = new BooleanSetting("revanced_hide_player_flyout_stable_volume", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_VIDEO_QUALITY_FOOTER = new BooleanSetting("revanced_hide_player_flyout_video_quality_footer", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_VIDEO_QUALITY = new BooleanSetting("revanced_hide_player_flyout_video_quality", FALSE);
    public static final BooleanSetting HIDE_PLAYER_FLYOUT_WATCH_IN_VR = new BooleanSetting("revanced_hide_player_flyout_watch_in_vr", TRUE);

    // General layout
    public static final BooleanSetting RESTORE_OLD_SETTINGS_MENUS = new BooleanSetting("revanced_restore_old_settings_menus", FALSE, true);
    public static final EnumSetting<FormFactor> CHANGE_FORM_FACTOR = new EnumSetting<>("revanced_change_form_factor", FormFactor.DEFAULT, true, "revanced_change_form_factor_user_dialog_message");
    public static final BooleanSetting BYPASS_IMAGE_REGION_RESTRICTIONS = new BooleanSetting("revanced_bypass_image_region_restrictions", FALSE, true);
    public static final BooleanSetting GRADIENT_LOADING_SCREEN = new BooleanSetting("revanced_gradient_loading_screen", FALSE, true);
    public static final EnumSetting<SplashScreenAnimationStyle> SPLASH_SCREEN_ANIMATION_STYLE = new EnumSetting<>("revanced_splash_screen_animation_style", SplashScreenAnimationStyle.FPS_60_ONE_SECOND, true);
    public static final EnumSetting<HeaderLogo> HEADER_LOGO = new EnumSetting<>("revanced_header_logo", HeaderLogo.DEFAULT, true);
    public static final BooleanSetting DISABLE_SIGNIN_TO_TV_POPUP = new BooleanSetting("revanced_disable_signin_to_tv_popup", FALSE);

    public static final BooleanSetting REMOVE_VIEWER_DISCRETION_DIALOG = new BooleanSetting("revanced_remove_viewer_discretion_dialog", FALSE,
            "revanced_remove_viewer_discretion_dialog_user_dialog_message");
    public static final BooleanSetting SPOOF_APP_VERSION = new BooleanSetting("revanced_spoof_app_version", FALSE, true, "revanced_spoof_app_version_user_dialog_message");
    public static final BooleanSetting WIDE_SEARCHBAR = new BooleanSetting("revanced_wide_searchbar", FALSE, true);
    public static final EnumSetting<StartPage> CHANGE_START_PAGE = new EnumSetting<>("revanced_change_start_page", StartPage.DEFAULT, true);
    public static final BooleanSetting CHANGE_START_PAGE_ALWAYS = new BooleanSetting("revanced_change_start_page_always", FALSE, true,
            new ChangeStartPageTypeAvailability());
    public static final StringSetting SPOOF_APP_VERSION_TARGET = new StringSetting("revanced_spoof_app_version_target", "19.01.34", true, parent(SPOOF_APP_VERSION));

    // Custom filter
    public static final BooleanSetting CUSTOM_FILTER = new BooleanSetting("revanced_custom_filter", FALSE);
    public static final StringSetting CUSTOM_FILTER_STRINGS = new StringSetting("revanced_custom_filter_strings", "", true, parent(CUSTOM_FILTER));

    // Navigation buttons
    public static final BooleanSetting HIDE_HOME_BUTTON = new BooleanSetting("revanced_hide_home_button", FALSE, true);
    public static final BooleanSetting HIDE_CREATE_BUTTON = new BooleanSetting("revanced_hide_create_button", TRUE, true);
    public static final BooleanSetting HIDE_SHORTS_BUTTON = new BooleanSetting("revanced_hide_shorts_button", TRUE, true);
    public static final BooleanSetting HIDE_SUBSCRIPTIONS_BUTTON = new BooleanSetting("revanced_hide_subscriptions_button", FALSE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BUTTON_LABELS = new BooleanSetting("revanced_hide_navigation_button_labels", FALSE, true);
    public static final BooleanSetting HIDE_NOTIFICATIONS_BUTTON = new BooleanSetting("revanced_hide_notifications_button", FALSE, true);
    public static final BooleanSetting SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON = new BooleanSetting("revanced_switch_create_with_notifications_button", TRUE, true,
            "revanced_switch_create_with_notifications_button_user_dialog_message");
    public static final BooleanSetting DISABLE_TRANSLUCENT_STATUS_BAR = new BooleanSetting("revanced_disable_translucent_status_bar", FALSE, true,
            "revanced_disable_translucent_status_bar_user_dialog_message");
    public static final BooleanSetting DISABLE_TRANSLUCENT_NAVIGATION_BAR_LIGHT = new BooleanSetting("revanced_disable_translucent_navigation_bar_light", FALSE, true);
    public static final BooleanSetting DISABLE_TRANSLUCENT_NAVIGATION_BAR_DARK = new BooleanSetting("revanced_disable_translucent_navigation_bar_dark", FALSE, true);

    // Shorts
    public static final BooleanSetting DISABLE_RESUMING_SHORTS_PLAYER = new BooleanSetting("revanced_disable_resuming_shorts_player", FALSE);
    public static final BooleanSetting DISABLE_SHORTS_BACKGROUND_PLAYBACK = new BooleanSetting("revanced_shorts_disable_background_playback", FALSE);
    public static final EnumSetting<ShortsPlayerType> SHORTS_PLAYER_TYPE = new EnumSetting<>("revanced_shorts_player_type", ShortsPlayerType.SHORTS_PLAYER);
    public static final BooleanSetting HIDE_SHORTS_AUTO_DUBBED_LABEL = new BooleanSetting("revanced_hide_shorts_auto_dubbed_label", FALSE);
    public static final BooleanSetting HIDE_SHORTS_CHANNEL_BAR = new BooleanSetting("revanced_hide_shorts_channel_bar", FALSE);
    public static final BooleanSetting HIDE_SHORTS_COMMENTS_BUTTON = new BooleanSetting("revanced_hide_shorts_comments_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_DISLIKE_BUTTON = new BooleanSetting("revanced_hide_shorts_dislike_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_FULL_VIDEO_LINK_LABEL = new BooleanSetting("revanced_hide_shorts_full_video_link_label", FALSE);
    public static final BooleanSetting HIDE_SHORTS_EFFECT_BUTTON = new BooleanSetting("revanced_hide_shorts_effect_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_GREEN_SCREEN_BUTTON = new BooleanSetting("revanced_hide_shorts_green_screen_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_NEW_POSTS_BUTTON = new BooleanSetting("revanced_hide_shorts_new_posts_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_HASHTAG_BUTTON = new BooleanSetting("revanced_hide_shorts_hashtag_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_HISTORY = new BooleanSetting("revanced_hide_shorts_history", FALSE);
    public static final BooleanSetting HIDE_SHORTS_HOME = new BooleanSetting("revanced_hide_shorts_home", FALSE);
    public static final BooleanSetting HIDE_SHORTS_INFO_PANEL = new BooleanSetting("revanced_hide_shorts_info_panel", TRUE);
    public static final BooleanSetting HIDE_SHORTS_JOIN_BUTTON = new BooleanSetting("revanced_hide_shorts_join_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_LIKE_BUTTON = new BooleanSetting("revanced_hide_shorts_like_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_LIKE_FOUNTAIN = new BooleanSetting("revanced_hide_shorts_like_fountain", TRUE);
    public static final BooleanSetting HIDE_SHORTS_LIVE_PREVIEW = new BooleanSetting("revanced_hide_shorts_live_preview", FALSE);
    public static final BooleanSetting HIDE_SHORTS_LOCATION_LABEL = new BooleanSetting("revanced_hide_shorts_location_label", FALSE);
    public static final BooleanSetting HIDE_SHORTS_NAVIGATION_BAR = new BooleanSetting("revanced_hide_shorts_navigation_bar", FALSE, true);
    public static final BooleanSetting HIDE_SHORTS_PAUSED_OVERLAY_BUTTONS = new BooleanSetting("revanced_hide_shorts_paused_overlay_buttons", FALSE);
    public static final BooleanSetting HIDE_SHORTS_PREVIEW_COMMENT = new BooleanSetting("revanced_hide_shorts_preview_comment", TRUE);
    public static final BooleanSetting HIDE_SHORTS_REMIX_BUTTON = new BooleanSetting("revanced_hide_shorts_remix_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SAVE_SOUND_BUTTON = new BooleanSetting("revanced_hide_shorts_save_sound_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_SEARCH = new BooleanSetting("revanced_hide_shorts_search", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SEARCH_SUGGESTIONS = new BooleanSetting("revanced_hide_shorts_search_suggestions", TRUE);
    public static final BooleanSetting HIDE_SHORTS_SHARE_BUTTON = new BooleanSetting("revanced_hide_shorts_share_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SHOP_BUTTON = new BooleanSetting("revanced_hide_shorts_shop_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_SOUND_BUTTON = new BooleanSetting("revanced_hide_shorts_sound_button", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SOUND_METADATA_LABEL = new BooleanSetting("revanced_hide_shorts_sound_metadata_label", FALSE);
    public static final BooleanSetting HIDE_SHORTS_STICKERS = new BooleanSetting("revanced_hide_shorts_stickers", TRUE);
    public static final BooleanSetting HIDE_SHORTS_SUBSCRIBE_BUTTON = new BooleanSetting("revanced_hide_shorts_subscribe_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_SUBSCRIPTIONS = new BooleanSetting("revanced_hide_shorts_subscriptions", FALSE);
    public static final BooleanSetting HIDE_SHORTS_SUPER_THANKS_BUTTON = new BooleanSetting("revanced_hide_shorts_super_thanks_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_TAGGED_PRODUCTS = new BooleanSetting("revanced_hide_shorts_tagged_products", TRUE);
    public static final BooleanSetting HIDE_SHORTS_UPCOMING_BUTTON = new BooleanSetting("revanced_hide_shorts_upcoming_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_USE_SOUND_BUTTON = new BooleanSetting("revanced_hide_shorts_use_sound_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_USE_TEMPLATE_BUTTON = new BooleanSetting("revanced_hide_shorts_use_template_button", TRUE);
    public static final BooleanSetting HIDE_SHORTS_VIDEO_TITLE = new BooleanSetting("revanced_hide_shorts_video_title", FALSE);
    public static final BooleanSetting SHORTS_AUTOPLAY = new BooleanSetting("revanced_shorts_autoplay", FALSE);
    public static final BooleanSetting SHORTS_AUTOPLAY_BACKGROUND = new BooleanSetting("revanced_shorts_autoplay_background", TRUE);

    // Seekbar
    public static final BooleanSetting DISABLE_PRECISE_SEEKING_GESTURE = new BooleanSetting("revanced_disable_precise_seeking_gesture", FALSE);
    public static final BooleanSetting HIDE_SEEKBAR = new BooleanSetting("revanced_hide_seekbar", FALSE, true);
    public static final BooleanSetting HIDE_SEEKBAR_THUMBNAIL = new BooleanSetting("revanced_hide_seekbar_thumbnail", FALSE, true);
    public static final BooleanSetting HIDE_TIMESTAMP = new BooleanSetting("revanced_hide_timestamp", FALSE);
    public static final BooleanSetting RESTORE_OLD_SEEKBAR_THUMBNAILS = new BooleanSetting("revanced_restore_old_seekbar_thumbnails", TRUE);
    public static final BooleanSetting SEEKBAR_TAPPING = new BooleanSetting("revanced_seekbar_tapping", FALSE);
    public static final BooleanSetting SEEKBAR_THUMBNAILS_HIGH_QUALITY = new BooleanSetting("revanced_seekbar_thumbnails_high_quality", FALSE, true,
            "revanced_seekbar_thumbnails_high_quality_dialog_message", new SeekbarThumbnailsHighQualityAvailability());
    public static final BooleanSetting SLIDE_TO_SEEK = new BooleanSetting("revanced_slide_to_seek", FALSE, true);
    public static final BooleanSetting SEEKBAR_CUSTOM_COLOR = new BooleanSetting("revanced_seekbar_custom_color", FALSE, true);
    public static final StringSetting SEEKBAR_CUSTOM_COLOR_PRIMARY = new StringSetting("revanced_seekbar_custom_color_primary", "#FF0033", true, parent(SEEKBAR_CUSTOM_COLOR));
    public static final StringSetting SEEKBAR_CUSTOM_COLOR_ACCENT = new StringSetting("revanced_seekbar_custom_color_accent", "#FF2791", true, parent(SEEKBAR_CUSTOM_COLOR));

    // Miscellaneous
    public static final BooleanSetting ANNOUNCEMENTS = new BooleanSetting("revanced_announcements", TRUE);
    public static final IntegerSetting ANNOUNCEMENT_LAST_ID = new IntegerSetting("revanced_announcement_last_id", -1, false, false);
    public static final BooleanSetting LOOP_VIDEO = new BooleanSetting("revanced_loop_video", FALSE);
    public static final BooleanSetting LOOP_VIDEO_BUTTON = new BooleanSetting("revanced_loop_video_button", FALSE);
    public static final BooleanSetting BYPASS_URL_REDIRECTS = new BooleanSetting("revanced_bypass_url_redirects", TRUE);
    public static final BooleanSetting DISABLE_HAPTIC_FEEDBACK_CHAPTERS = new BooleanSetting("revanced_disable_haptic_feedback_chapters", FALSE);
    public static final BooleanSetting DISABLE_HAPTIC_FEEDBACK_PRECISE_SEEKING = new BooleanSetting("revanced_disable_haptic_feedback_precise_seeking", FALSE);
    public static final BooleanSetting DISABLE_HAPTIC_FEEDBACK_SEEK_UNDO = new BooleanSetting("revanced_disable_haptic_feedback_seek_undo", FALSE);
    public static final BooleanSetting DISABLE_HAPTIC_FEEDBACK_ZOOM = new BooleanSetting("revanced_disable_haptic_feedback_zoom", FALSE);
    public static final BooleanSetting EXTERNAL_BROWSER = new BooleanSetting("revanced_external_browser", TRUE, true);
    public static final BooleanSetting SPOOF_DEVICE_DIMENSIONS = new BooleanSetting("revanced_spoof_device_dimensions", FALSE, true,
            "revanced_spoof_device_dimensions_user_dialog_message");
    public static final EnumSetting<ClientType> SPOOF_VIDEO_STREAMS_CLIENT_TYPE = new EnumSetting<>("revanced_spoof_video_streams_client_type", ClientType.ANDROID_VR_1_43_32, true, parent(SPOOF_VIDEO_STREAMS));
    public static final BooleanSetting SPOOF_VIDEO_STREAMS_AV1 = new BooleanSetting("revanced_spoof_video_streams_av1", FALSE, true,
            "revanced_spoof_video_streams_av1_user_dialog_message", new SpoofClientAv1Availability());
    public static final BooleanSetting DEBUG_PROTOBUFFER = new BooleanSetting("revanced_debug_protobuffer", FALSE, false,
            "revanced_debug_protobuffer_user_dialog_message", parent(BaseSettings.DEBUG));

    // Swipe controls
    public static final BooleanSetting SWIPE_CHANGE_VIDEO = new BooleanSetting("revanced_swipe_change_video", FALSE, true);
    public static final BooleanSetting SWIPE_BRIGHTNESS = new BooleanSetting("revanced_swipe_brightness", FALSE, true);
    public static final BooleanSetting SWIPE_VOLUME = new BooleanSetting("revanced_swipe_volume", FALSE, true);
    public static final BooleanSetting SWIPE_PRESS_TO_ENGAGE = new BooleanSetting("revanced_swipe_press_to_engage", FALSE, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final BooleanSetting SWIPE_HAPTIC_FEEDBACK = new BooleanSetting("revanced_swipe_haptic_feedback", TRUE, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final IntegerSetting SWIPE_MAGNITUDE_THRESHOLD = new IntegerSetting("revanced_swipe_threshold", 30, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final IntegerSetting SWIPE_VOLUME_SENSITIVITY = new IntegerSetting("revanced_swipe_volume_sensitivity", 1, true, parent(SWIPE_VOLUME));
    public static final EnumSetting<SwipeOverlayStyle> SWIPE_OVERLAY_STYLE = new EnumSetting<>("revanced_swipe_overlay_style", SwipeOverlayStyle.HORIZONTAL,true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final IntegerSetting SWIPE_OVERLAY_TEXT_SIZE = new IntegerSetting("revanced_swipe_text_overlay_size", 14, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final IntegerSetting SWIPE_OVERLAY_OPACITY = new IntegerSetting("revanced_swipe_overlay_background_opacity", 60, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final StringSetting SWIPE_OVERLAY_BRIGHTNESS_COLOR = new StringSetting("revanced_swipe_overlay_progress_brightness_color", "#BFFFFFFF", true,
            parent(SWIPE_BRIGHTNESS));
    public static final StringSetting SWIPE_OVERLAY_VOLUME_COLOR = new StringSetting("revanced_swipe_overlay_progress_volume_color", "#BFFFFFFF", true,
            parent(SWIPE_VOLUME));
    public static final LongSetting SWIPE_OVERLAY_TIMEOUT = new LongSetting("revanced_swipe_overlay_timeout", 500L, true,
            parentsAny(SWIPE_BRIGHTNESS, SWIPE_VOLUME));
    public static final BooleanSetting SWIPE_SAVE_AND_RESTORE_BRIGHTNESS = new BooleanSetting("revanced_swipe_save_and_restore_brightness", TRUE, true,
            parent(SWIPE_BRIGHTNESS));
    public static final FloatSetting SWIPE_BRIGHTNESS_VALUE = new FloatSetting("revanced_swipe_brightness_value", -1f);
    public static final BooleanSetting SWIPE_LOWEST_VALUE_ENABLE_AUTO_BRIGHTNESS = new BooleanSetting("revanced_swipe_lowest_value_enable_auto_brightness", FALSE, true,
            parent(SWIPE_BRIGHTNESS));

    // ReturnYoutubeDislike
    public static final BooleanSetting RYD_ENABLED = new BooleanSetting("revanced_ryd_enabled", TRUE);
    public static final StringSetting RYD_USER_ID = new StringSetting("revanced_ryd_user_id", "", false, false);
    public static final BooleanSetting RYD_SHORTS = new BooleanSetting("revanced_ryd_shorts", TRUE, parent(RYD_ENABLED));
    public static final BooleanSetting RYD_DISLIKE_PERCENTAGE = new BooleanSetting("revanced_ryd_dislike_percentage", FALSE, true, parent(RYD_ENABLED));
    public static final BooleanSetting RYD_COMPACT_LAYOUT = new BooleanSetting("revanced_ryd_compact_layout", FALSE, true, parent(RYD_ENABLED));
    public static final BooleanSetting RYD_ESTIMATED_LIKE = new BooleanSetting("revanced_ryd_estimated_like", TRUE, true, parent(RYD_ENABLED));
    public static final BooleanSetting RYD_TOAST_ON_CONNECTION_ERROR = new BooleanSetting("revanced_ryd_toast_on_connection_error", TRUE, parent(RYD_ENABLED));

    // SponsorBlock
    public static final BooleanSetting SB_ENABLED = new BooleanSetting("sb_enabled", TRUE);
    /** Do not use id setting directly. Instead use {@link SponsorBlockSettings}. */
    public static final StringSetting SB_PRIVATE_USER_ID = new StringSetting("sb_private_user_id_Do_Not_Share", "", parent(SB_ENABLED));
    public static final IntegerSetting SB_CREATE_NEW_SEGMENT_STEP = new IntegerSetting("sb_create_new_segment_step", 150, parent(SB_ENABLED));
    public static final BooleanSetting SB_VOTING_BUTTON = new BooleanSetting("sb_voting_button", FALSE, parent(SB_ENABLED));
    public static final BooleanSetting SB_CREATE_NEW_SEGMENT = new BooleanSetting("sb_create_new_segment", FALSE, parent(SB_ENABLED));
    public static final BooleanSetting SB_SQUARE_LAYOUT = new BooleanSetting("sb_square_layout", FALSE, parent(SB_ENABLED));
    public static final BooleanSetting SB_COMPACT_SKIP_BUTTON = new BooleanSetting("sb_compact_skip_button", FALSE, parent(SB_ENABLED));
    public static final BooleanSetting SB_AUTO_HIDE_SKIP_BUTTON = new BooleanSetting("sb_auto_hide_skip_button", TRUE, parent(SB_ENABLED));
    public static final EnumSetting<SponsorBlockDuration> SB_AUTO_HIDE_SKIP_BUTTON_DURATION = new EnumSetting<>("sb_auto_hide_skip_button_duration",
            SponsorBlockDuration.FOUR_SECONDS, parent(SB_ENABLED));
    public static final BooleanSetting SB_TOAST_ON_SKIP = new BooleanSetting("sb_toast_on_skip", TRUE, parent(SB_ENABLED));
    public static final EnumSetting<SponsorBlockDuration> SB_TOAST_ON_SKIP_DURATION = new EnumSetting<>("sb_toast_on_skip_duration",
            SponsorBlockDuration.FOUR_SECONDS, parentsAll(SB_ENABLED, SB_TOAST_ON_SKIP));
    public static final BooleanSetting SB_TOAST_ON_CONNECTION_ERROR = new BooleanSetting("sb_toast_on_connection_error", TRUE, parent(SB_ENABLED));
    public static final BooleanSetting SB_TRACK_SKIP_COUNT = new BooleanSetting("sb_track_skip_count", TRUE, parent(SB_ENABLED));
    public static final FloatSetting SB_SEGMENT_MIN_DURATION = new FloatSetting("sb_min_segment_duration", 0F, parent(SB_ENABLED));
    public static final BooleanSetting SB_VIDEO_LENGTH_WITHOUT_SEGMENTS = new BooleanSetting("sb_video_length_without_segments", FALSE, parent(SB_ENABLED));
    public static final StringSetting SB_API_URL = new StringSetting("sb_api_url", "https://sponsor.ajay.app", parent(SB_ENABLED));
    public static final BooleanSetting SB_USER_IS_VIP = new BooleanSetting("sb_user_is_vip", FALSE);
    public static final IntegerSetting SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS = new IntegerSetting("sb_local_time_saved_number_segments", 0, parent(SB_ENABLED));
    public static final LongSetting SB_LOCAL_TIME_SAVED_MILLISECONDS = new LongSetting("sb_local_time_saved_milliseconds", 0L, parent(SB_ENABLED));
    public static final LongSetting SB_LAST_VIP_CHECK = new LongSetting("sb_last_vip_check", 0L, false, false);
    public static final BooleanSetting SB_HIDE_EXPORT_WARNING = new BooleanSetting("sb_hide_export_warning", FALSE, false, false);
    public static final BooleanSetting SB_SEEN_GUIDELINES = new BooleanSetting("sb_seen_guidelines", FALSE, false, false);
    public static final StringSetting SB_CATEGORY_SPONSOR = new StringSetting("sb_sponsor", SKIP_AUTOMATICALLY_ONCE.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_SPONSOR_COLOR = new StringSetting("sb_sponsor_color", "#CC00D400");
    public static final StringSetting SB_CATEGORY_SELF_PROMO = new StringSetting("sb_selfpromo", MANUAL_SKIP.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_SELF_PROMO_COLOR = new StringSetting("sb_selfpromo_color", "#CCFFFF00");
    public static final StringSetting SB_CATEGORY_INTERACTION = new StringSetting("sb_interaction", MANUAL_SKIP.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_INTERACTION_COLOR = new StringSetting("sb_interaction_color", "#CCCC00FF");
    public static final StringSetting SB_CATEGORY_HIGHLIGHT = new StringSetting("sb_highlight", MANUAL_SKIP.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_HIGHLIGHT_COLOR = new StringSetting("sb_highlight_color", "#CCFF1684");
    public static final StringSetting SB_CATEGORY_HOOK = new StringSetting("sb_hook", IGNORE.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_HOOK_COLOR = new StringSetting("sb_hook_color", "#CC395699");
    public static final StringSetting SB_CATEGORY_INTRO = new StringSetting("sb_intro", MANUAL_SKIP.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_INTRO_COLOR = new StringSetting("sb_intro_color", "#CC00FFFF");
    public static final StringSetting SB_CATEGORY_OUTRO = new StringSetting("sb_outro", MANUAL_SKIP.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_OUTRO_COLOR = new StringSetting("sb_outro_color", "#CC0202ED");
    public static final StringSetting SB_CATEGORY_PREVIEW = new StringSetting("sb_preview", IGNORE.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_PREVIEW_COLOR = new StringSetting("sb_preview_color", "#CC008FD6");
    public static final StringSetting SB_CATEGORY_FILLER = new StringSetting("sb_filler", IGNORE.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_FILLER_COLOR = new StringSetting("sb_filler_color", "#CC7300FF");
    public static final StringSetting SB_CATEGORY_MUSIC_OFFTOPIC = new StringSetting("sb_music_offtopic", MANUAL_SKIP.reVancedKeyValue, parent(SB_ENABLED));
    public static final StringSetting SB_CATEGORY_MUSIC_OFFTOPIC_COLOR = new StringSetting("sb_music_offtopic_color", "#CCFF9900");
    // Dummy setting. Category is not exposed in the UI nor does it ever change.
    public static final StringSetting SB_CATEGORY_UNSUBMITTED = new StringSetting("sb_unsubmitted", SKIP_AUTOMATICALLY.reVancedKeyValue, false, false);
    public static final StringSetting SB_CATEGORY_UNSUBMITTED_COLOR = new StringSetting("sb_unsubmitted_color", "#FFFFFFFF", false, false);

    // Deprecated migrations
    private static final StringSetting  DEPRECATED_SEEKBAR_CUSTOM_COLOR_PRIMARY = new StringSetting("revanced_seekbar_custom_color_value", "#FF0033");

    private static final FloatSetting DEPRECATED_SB_CATEGORY_SPONSOR_OPACITY = new FloatSetting("sb_sponsor_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_SELF_PROMO_OPACITY = new FloatSetting("sb_selfpromo_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_INTERACTION_OPACITY = new FloatSetting("sb_interaction_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_HIGHLIGHT_OPACITY = new FloatSetting("sb_highlight_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_HOOK_OPACITY = new FloatSetting("sb_hook_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_INTRO_OPACITY = new FloatSetting("sb_intro_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_OUTRO_OPACITY = new FloatSetting("sb_outro_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_PREVIEW_OPACITY = new FloatSetting("sb_preview_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_FILLER_OPACITY = new FloatSetting("sb_filler_opacity", 0.8f, false, false);
    private static final FloatSetting DEPRECATED_SB_CATEGORY_MUSIC_OFFTOPIC_OPACITY = new FloatSetting("sb_music_offtopic_opacity", 0.8f, false, false);

    static {
        // region Migration

        // Migrate renamed change header enums.
        if (HEADER_LOGO.get() == HeaderLogo.REVANCED) {
            HEADER_LOGO.save(HeaderLogo.ROUNDED);
        }
        if (HEADER_LOGO.get() == HeaderLogo.REVANCED_MINIMAL) {
            HEADER_LOGO.save(HeaderLogo.MINIMAL);
        }

        // Migrate old single color seekbar with a slightly brighter accent color based on the primary.
        // Eventually delete this logic.
        if (!DEPRECATED_SEEKBAR_CUSTOM_COLOR_PRIMARY.isSetToDefault()) {
            try {
                String oldPrimaryColorString = DEPRECATED_SEEKBAR_CUSTOM_COLOR_PRIMARY.get();
                final int oldPrimaryColor = Color.parseColor(oldPrimaryColorString);
                SEEKBAR_CUSTOM_COLOR_PRIMARY.save(oldPrimaryColorString);

                final float brightnessScale = 1.3f;
                final int accentColor = Color.argb(
                        0, // Save without alpha channel.
                        Math.min(255, (int) (brightnessScale * Color.red(oldPrimaryColor))),
                        Math.min(255, (int) (brightnessScale * Color.green(oldPrimaryColor))),
                        Math.min(255, (int) (brightnessScale * Color.blue(oldPrimaryColor)))
                );

                SEEKBAR_CUSTOM_COLOR_ACCENT.save(String.format("#%06X", accentColor));
            } catch (Exception ex) {
                Logger.printException(() -> "Could not parse old seekbar color", ex);
            }

            DEPRECATED_SEEKBAR_CUSTOM_COLOR_PRIMARY.resetToDefault();
        }

        // Old spoof versions that no longer work,
        // or is spoofing to a version the same or newer than this app.
        if (!SPOOF_APP_VERSION_TARGET.isSetToDefault() &&
                (SPOOF_APP_VERSION_TARGET.get().compareTo(SPOOF_APP_VERSION_TARGET.defaultValue) < 0
                || (Utils.getAppVersionName().compareTo(SPOOF_APP_VERSION_TARGET.get()) <= 0))) {
            Logger.printInfo(() -> "Resetting spoof app version");
            SPOOF_APP_VERSION_TARGET.resetToDefault();
            SPOOF_APP_VERSION.resetToDefault();
        }

        // VR 1.61 is not selectable in the settings, and it's selected by spoof stream patch if needed.
        if (SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get() == ClientType.ANDROID_VR_1_61_48) {
            SPOOF_VIDEO_STREAMS_CLIENT_TYPE.resetToDefault();
        }

        // RYD requires manually migrating old settings since the lack of
        // a "revanced_" on the old setting causes duplicate key exceptions during export.
        Setting.migrateFromOldPreferences(Setting.preferences, RYD_USER_ID, "ryd_user_id");

        // Migrate old saved data. Must be done here before the settings can be used by any other code.
        applyOldSbOpacityToColor(SB_CATEGORY_SPONSOR_COLOR, DEPRECATED_SB_CATEGORY_SPONSOR_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_SELF_PROMO_COLOR, DEPRECATED_SB_CATEGORY_SELF_PROMO_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_INTERACTION_COLOR, DEPRECATED_SB_CATEGORY_INTERACTION_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_HIGHLIGHT_COLOR, DEPRECATED_SB_CATEGORY_HIGHLIGHT_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_HOOK_COLOR, DEPRECATED_SB_CATEGORY_HOOK_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_INTRO_COLOR, DEPRECATED_SB_CATEGORY_INTRO_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_OUTRO_COLOR, DEPRECATED_SB_CATEGORY_OUTRO_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_PREVIEW_COLOR, DEPRECATED_SB_CATEGORY_PREVIEW_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_FILLER_COLOR, DEPRECATED_SB_CATEGORY_FILLER_OPACITY);
        applyOldSbOpacityToColor(SB_CATEGORY_MUSIC_OFFTOPIC_COLOR, DEPRECATED_SB_CATEGORY_MUSIC_OFFTOPIC_OPACITY);

        // endregion

        // region SB import/export callbacks

        Setting.addImportExportCallback(SponsorBlockSettings.SB_IMPORT_EXPORT_CALLBACK);

        // endregion
    }

    private static void applyOldSbOpacityToColor(StringSetting colorSetting, FloatSetting opacitySetting) {
        String colorString = colorSetting.get();
        if (colorString.length() >= 8) {
            return; // Color is already #ARGB
        }

        colorSetting.save(SponsorBlockSettings.migrateOldColorString(colorString, opacitySetting.get()));
    }
}
