package app.revanced.integrations.twitch.settings;

import app.revanced.integrations.shared.settings.BooleanSetting;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.shared.settings.StringSetting;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Settings extends BaseSettings {
    /* Ads */
    public static final BooleanSetting BLOCK_VIDEO_ADS = new BooleanSetting("revanced_block_video_ads", TRUE);
    public static final BooleanSetting BLOCK_AUDIO_ADS = new BooleanSetting("revanced_block_audio_ads", TRUE);
    public static final StringSetting BLOCK_EMBEDDED_ADS = new StringSetting("revanced_block_embedded_ads", "luminous");

    /* Chat */
    public static final StringSetting SHOW_DELETED_MESSAGES = new StringSetting("revanced_show_deleted_messages", "cross-out");
    public static final BooleanSetting AUTO_CLAIM_CHANNEL_POINTS = new BooleanSetting("revanced_auto_claim_channel_points", TRUE);

    /* Misc */
    /**
     * Not to be confused with {@link BaseSettings#DEBUG}.
     */
    public static final BooleanSetting TWITCH_DEBUG_MODE = new BooleanSetting("revanced_twitch_debug_mode", FALSE, true);
}
