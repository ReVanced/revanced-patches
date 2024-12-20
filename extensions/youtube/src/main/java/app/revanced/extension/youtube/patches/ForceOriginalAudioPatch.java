package app.revanced.extension.youtube.patches;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.EnumSetting;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.spoof.ClientType;
import app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ForceOriginalAudioPatch {

    private static final String DEFAULT_AUDIO_TRACKS_SUFFIX = ".4";

    public static final class ForceOriginalAudioAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            if (SpoofVideoStreamsPatch.isPatchIncluded()) {
                // Force audio does not work with Android VR.
                EnumSetting<ClientType> clientType = BaseSettings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE;
                return clientType.isAvailable() && clientType.get() != ClientType.ANDROID_VR;
            }

            return true;
        }
    }

    /**
     * Injection point.
     */
    public static boolean isDefaultAudioStream(boolean isDefault, String audioTrackId, String audioTrackDisplayName) {
        try {
            if (!Settings.FORCE_ORIGINAL_AUDIO.get()) {
                return isDefault;
            }

            if (audioTrackId.isEmpty()) {
                // Older app targets can have empty audio tracks and these might be placeholders.
                // The real audio tracks are called after these.
                return isDefault;
            }

            Logger.printDebug(() -> "default: " + String.format("%-5s", isDefault) + " id: "
                    + String.format("%-8s", audioTrackId) + " name:" + audioTrackDisplayName);

            final boolean isOriginal = audioTrackId.endsWith(DEFAULT_AUDIO_TRACKS_SUFFIX);
            if (isOriginal) {
                Logger.printDebug(() -> "Using audio: " + audioTrackId);
            }

            return isOriginal;
        } catch (Exception ex) {
            Logger.printException(() -> "isDefaultAudioStream failure", ex);

            return isDefault;
        }
    }
}
