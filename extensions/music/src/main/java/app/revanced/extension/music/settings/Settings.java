package app.revanced.extension.music.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import static app.revanced.extension.shared.settings.Setting.parent;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.EnumSetting;
import app.revanced.extension.shared.spoof.ClientType;

public class Settings extends BaseSettings {

    // Ads
    public static final BooleanSetting HIDE_VIDEO_ADS = new BooleanSetting("revanced_music_hide_video_ads", TRUE, true);
    public static final BooleanSetting HIDE_GET_PREMIUM_LABEL = new BooleanSetting("revanced_music_hide_get_premium_label", TRUE, true);
    public static final BooleanSetting HIDE_UPGRADE_BUTTON = new BooleanSetting("revanced_music_hide_upgrade_button", TRUE, true);

    // General
    public static final BooleanSetting HIDE_CATEGORY_BAR = new BooleanSetting("revanced_music_hide_category_bar", FALSE, true);

    // Player
    public static final BooleanSetting PERMANENT_REPEAT = new BooleanSetting("revanced_music_play_permanent_repeat", FALSE, true);

    // Miscellaneous
    public static final EnumSetting<ClientType> SPOOF_VIDEO_STREAMS_CLIENT_TYPE = new EnumSetting<>("revanced_spoof_video_streams_client_type",
            ClientType.ANDROID_VR_1_43_32, true, parent(SPOOF_VIDEO_STREAMS));
}
