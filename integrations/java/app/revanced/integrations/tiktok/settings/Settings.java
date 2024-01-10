package app.revanced.integrations.tiktok.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.shared.settings.BooleanSetting;
import app.revanced.integrations.shared.settings.FloatSetting;
import app.revanced.integrations.shared.settings.StringSetting;

public class Settings extends BaseSettings {
    public static final BooleanSetting REMOVE_ADS = new BooleanSetting("remove_ads", TRUE, true);
    public static final BooleanSetting HIDE_LIVE = new BooleanSetting("hide_live", FALSE, true);
    public static final BooleanSetting HIDE_STORY = new BooleanSetting("hide_story", FALSE, true);
    public static final BooleanSetting HIDE_IMAGE = new BooleanSetting("hide_image", FALSE, true);
    public static final StringSetting MIN_MAX_VIEWS = new StringSetting("min_max_views", "0-" + Long.MAX_VALUE, true);
    public static final StringSetting MIN_MAX_LIKES = new StringSetting("min_max_likes", "0-" + Long.MAX_VALUE, true);
    public static final StringSetting DOWNLOAD_PATH = new StringSetting("down_path", "DCIM/TikTok");
    public static final BooleanSetting DOWNLOAD_WATERMARK = new BooleanSetting("down_watermark", TRUE);
    public static final BooleanSetting CLEAR_DISPLAY = new BooleanSetting("clear_display", FALSE);
    public static final FloatSetting REMEMBERED_SPEED = new FloatSetting("REMEMBERED_SPEED", 1.0f);
    public static final BooleanSetting SIM_SPOOF = new BooleanSetting("simspoof", TRUE, true);
    public static final StringSetting SIM_SPOOF_ISO = new StringSetting("simspoof_iso", "us");
    public static final StringSetting SIMSPOOF_MCCMNC = new StringSetting("simspoof_mccmnc", "310160");
    public static final StringSetting SIMSPOOF_OP_NAME = new StringSetting("simspoof_op_name", "T-Mobile");
}
