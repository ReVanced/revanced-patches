package app.revanced.extension.shared.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.extension.shared.settings.Setting.parent;
import static app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch.SpoofiOSAvailability;

import app.revanced.extension.shared.spoof.AudioStreamLanguage;
import app.revanced.extension.shared.spoof.ClientType;

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

    public static final BooleanSetting SPOOF_VIDEO_STREAMS = new BooleanSetting("revanced_spoof_video_streams", TRUE, true, "revanced_spoof_video_streams_user_dialog_message");
    public static final EnumSetting<AudioStreamLanguage> SPOOF_VIDEO_STREAMS_LANGUAGE = new EnumSetting<>("revanced_spoof_video_streams_language", AudioStreamLanguage.DEFAULT, parent(SPOOF_VIDEO_STREAMS));
    public static final BooleanSetting SPOOF_VIDEO_STREAMS_IOS_FORCE_AVC = new BooleanSetting("revanced_spoof_video_streams_ios_force_avc", FALSE, true,
            "revanced_spoof_video_streams_ios_force_avc_user_dialog_message", new SpoofiOSAvailability());
    public static final EnumSetting<ClientType> SPOOF_VIDEO_STREAMS_CLIENT_TYPE = new EnumSetting<>("revanced_spoof_video_streams_type", ClientType.ANDROID_VR, true, parent(SPOOF_VIDEO_STREAMS));

}
