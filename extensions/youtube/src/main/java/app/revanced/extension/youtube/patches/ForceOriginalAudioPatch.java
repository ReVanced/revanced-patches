package app.revanced.extension.youtube.patches;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.spoof.SpoofVideoStreamsPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ForceOriginalAudioPatch {

    private static final String DEFAULT_AUDIO_TRACKS_SUFFIX = ".4";

    /**
     * Injection point.
     */
    public static void setPreferredLanguage() {
        if (Settings.FORCE_ORIGINAL_AUDIO.get()
                && SpoofVideoStreamsPatch.spoofingToClientWithNoMultiAudioStreams()
                && !Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get().useAuth) {
            // If client spoofing does not use authentication and lacks multi-audio streams,
            // then can use any language code for the request and if that requested language is
            // not available YT uses the original audio language. Authenticated requests ignore
            // the language code and always use the account language. Use a language that is
            // not auto-dubbed by YouTube: https://support.google.com/youtube/answer/15569972
            // but the language is also supported natively by the Meta Quest device that
            // Android VR is spoofing.
            AppLanguage override = AppLanguage.SV;
            Logger.printDebug(() -> "Setting language override: " + override);
            SpoofVideoStreamsPatch.setLanguageOverride(override);
        }
    }

    /**
     * Injection point.
     */
    public static boolean ignoreDefaultAudioStream(boolean original) {
        if (Settings.FORCE_ORIGINAL_AUDIO.get()) {
            return false;
        }
        return original;
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
