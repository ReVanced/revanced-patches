package app.revanced.extension.music.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.BooleanSetting;

public class Settings extends BaseSettings {

    // Ads
    public static final BooleanSetting HIDE_VIDEO_ADS = new BooleanSetting("revanced_music_hide_video_ads", TRUE, true);
    public static final BooleanSetting HIDE_GET_PREMIUM_LABEL = new BooleanSetting("revanced_music_hide_get_premium_label", TRUE, true);
    public static final BooleanSetting HIDE_UPGRADE_BUTTON = new BooleanSetting("revanced_music_hide_upgrade_button", TRUE, true);

    // General
    public static final BooleanSetting HIDE_CATEGORY_BAR = new BooleanSetting("revanced_music_hide_category_bar", FALSE, true);

    // Player
    public static final BooleanSetting PERMANENT_REPEAT = new BooleanSetting("revanced_music_play_permanent_repeat", FALSE, true);
}
