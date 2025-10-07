package app.revanced.extension.shared.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.extension.shared.patches.CustomBrandingPatch.BrandingTheme;
import static app.revanced.extension.shared.settings.Setting.parent;
import static app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch.AudioStreamLanguageOverrideAvailability;

/**
 * Settings shared across multiple apps.
 * <p>
 * To ensure this class is loaded when the UI is created, app specific setting bundles should extend
 * or reference this class.
 */
public class BaseSettings {
    public static final BooleanSetting DEBUG = new BooleanSetting("revanced_debug", FALSE);
    public static final BooleanSetting DEBUG_STACKTRACE = new BooleanSetting("revanced_debug_stacktrace", FALSE, parent(DEBUG));
    public static final BooleanSetting DEBUG_TOAST_ON_ERROR = new BooleanSetting("revanced_debug_toast_on_error", TRUE, "revanced_debug_toast_on_error_user_dialog_message");

    public static final IntegerSetting CHECK_ENVIRONMENT_WARNINGS_ISSUED = new IntegerSetting("revanced_check_environment_warnings_issued", 0, true, false);

    public static final EnumSetting<AppLanguage> REVANCED_LANGUAGE = new EnumSetting<>("revanced_language", AppLanguage.DEFAULT, true, "revanced_language_user_dialog_message");

    /**
     * Use the icons declared in the preferences created during patching. If no icons or styles are declared then this setting does nothing.
     */
    public static final BooleanSetting SHOW_MENU_ICONS = new BooleanSetting("revanced_show_menu_icons", TRUE, true);

    public static final BooleanSetting SETTINGS_SEARCH_HISTORY = new BooleanSetting("revanced_settings_search_history", TRUE, true);
    public static final StringSetting SETTINGS_SEARCH_ENTRIES = new StringSetting("revanced_settings_search_entries", "");

    //
    // Settings shared by YouTube and YouTube Music.
    //

    public static final BooleanSetting SPOOF_VIDEO_STREAMS = new BooleanSetting("revanced_spoof_video_streams", TRUE, true, "revanced_spoof_video_streams_user_dialog_message");
    public static final EnumSetting<AppLanguage> SPOOF_VIDEO_STREAMS_LANGUAGE = new EnumSetting<>("revanced_spoof_video_streams_language", AppLanguage.DEFAULT, new AudioStreamLanguageOverrideAvailability());
    public static final BooleanSetting SPOOF_STREAMING_DATA_STATS_FOR_NERDS = new BooleanSetting("revanced_spoof_streaming_data_stats_for_nerds", TRUE, parent(SPOOF_VIDEO_STREAMS));

    public static final BooleanSetting SANITIZE_SHARED_LINKS = new BooleanSetting("revanced_sanitize_sharing_links", TRUE);
    public static final BooleanSetting REPLACE_MUSIC_LINKS_WITH_YOUTUBE = new BooleanSetting("revanced_replace_music_with_youtube", FALSE);

    public static final BooleanSetting CHECK_WATCH_HISTORY_DOMAIN_NAME = new BooleanSetting("revanced_check_watch_history_domain_name", TRUE, false, false);

    public static final EnumSetting<BrandingTheme> CUSTOM_BRANDING_ICON = new EnumSetting<>("revanced_custom_branding_icon", BrandingTheme.ORIGINAL, true);
    public static final IntegerSetting CUSTOM_BRANDING_NAME = new IntegerSetting("revanced_custom_branding_name", 1, true);
}
