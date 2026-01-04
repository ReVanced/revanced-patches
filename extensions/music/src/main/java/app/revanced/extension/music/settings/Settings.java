package app.revanced.extension.music.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.extension.shared.settings.Setting.parent;

import app.revanced.extension.shared.settings.YouTubeAndMusicSettings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.EnumSetting;
import app.revanced.extension.shared.spoof.ClientType;

public class Settings extends YouTubeAndMusicSettings {

    // Ads
    public static final BooleanSetting HIDE_VIDEO_ADS = new BooleanSetting("revanced_music_hide_video_ads", TRUE, true);
    public static final BooleanSetting HIDE_GET_PREMIUM_LABEL = new BooleanSetting("revanced_music_hide_get_premium_label", TRUE, true);

    // General
    public static final BooleanSetting HIDE_CAST_BUTTON = new BooleanSetting("revanced_music_hide_cast_button", TRUE, true);
    public static final BooleanSetting HIDE_CATEGORY_BAR = new BooleanSetting("revanced_music_hide_category_bar", FALSE, true);
    public static final BooleanSetting HIDE_HISTORY_BUTTON = new BooleanSetting("revanced_music_hide_history_button", FALSE, true);
    public static final BooleanSetting HIDE_SEARCH_BUTTON = new BooleanSetting("revanced_music_hide_search_button", FALSE, true);
    public static final BooleanSetting HIDE_NOTIFICATION_BUTTON = new BooleanSetting("revanced_music_hide_notification_button", FALSE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BAR_HOME_BUTTON = new BooleanSetting("revanced_music_hide_navigation_bar_home_button", FALSE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BAR_SAMPLES_BUTTON = new BooleanSetting("revanced_music_hide_navigation_bar_samples_button", FALSE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BAR_EXPLORE_BUTTON = new BooleanSetting("revanced_music_hide_navigation_bar_explore_button", FALSE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BAR_LIBRARY_BUTTON = new BooleanSetting("revanced_music_hide_navigation_bar_library_button", FALSE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BAR_UPGRADE_BUTTON = new BooleanSetting("revanced_music_hide_navigation_bar_upgrade_button", TRUE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BAR = new BooleanSetting("revanced_music_hide_navigation_bar", FALSE, true);
    public static final BooleanSetting HIDE_NAVIGATION_BAR_LABEL = new BooleanSetting("revanced_music_hide_navigation_bar_labels", FALSE, true);

    // Player
    public static final BooleanSetting CHANGE_MINIPLAYER_COLOR = new BooleanSetting("revanced_music_change_miniplayer_color", FALSE, true);
    public static final BooleanSetting PERMANENT_REPEAT = new BooleanSetting("revanced_music_play_permanent_repeat", FALSE, true);

    // Miscellaneous
    public static final EnumSetting<ClientType> SPOOF_VIDEO_STREAMS_CLIENT_TYPE = new EnumSetting<>("revanced_spoof_video_streams_client_type",
            ClientType.ANDROID_VR_1_43_32, true, parent(SPOOF_VIDEO_STREAMS));

    public static final BooleanSetting FORCE_ORIGINAL_AUDIO = new BooleanSetting("revanced_force_original_audio", TRUE, true);
}
