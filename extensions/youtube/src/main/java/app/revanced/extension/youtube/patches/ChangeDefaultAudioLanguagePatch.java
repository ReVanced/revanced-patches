package app.revanced.extension.youtube.patches;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.spoof.AudioStreamLanguage;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ChangeDefaultAudioLanguagePatch {

    private static final String DEFAULT_AUDIO_TRACKS_IDENTIFIER = "original";

    /**
     * Audio track identifier.
     *
     * Examples:
     *  fr-FR.10
     *  it.10
     */
    private static final Pattern AUDIO_TRACK_ID_PATTERN =
            Pattern.compile("^([a-z]{2})(-[A-Z]{2})?.*");

    /**
     * Injection point.
     */
    public static boolean isAudioStreamAsDefault(boolean isDefault, String audioTrackId, String audioTrackDisplayName) {
        try {
            if (audioTrackId.isEmpty()) {
                // Older app targets can have empty audio tracks and these might be placeholders.
                // The real audio tracks are called after these.
                return isDefault;
            }

            AudioStreamLanguage defaultLanguage = Settings.AUDIO_DEFAULT_LANGUAGE.get();
            if (defaultLanguage == AudioStreamLanguage.DEFAULT) {
                return isDefault; // Do nothing.
            }

            Logger.printDebug(() -> "default: " + String.format("%-5s", isDefault) + " id: "
                    + String.format("%-8s", audioTrackId) + " name:" + audioTrackDisplayName);

            if (defaultLanguage == AudioStreamLanguage.ORIGINAL) {
                final boolean isOriginal = audioTrackDisplayName.contains(DEFAULT_AUDIO_TRACKS_IDENTIFIER);
                if (isOriginal) {
                    Logger.printDebug(() -> "Using audio: " + audioTrackId);
                }

                return isOriginal;
            }

            Matcher matcher = AUDIO_TRACK_ID_PATTERN.matcher(audioTrackId);
            if (!matcher.matches()) {
                Logger.printException(() -> "Cannot set default audio, unknown track: " + audioTrackId);
                return isDefault;
            }

            String desiredIso639 = defaultLanguage.getIso639_1();
            if (desiredIso639.equals(matcher.group(1))
                || desiredIso639.equals(matcher.group(2))) {
                Logger.printDebug(() -> "Using audio: " + audioTrackId);
                return true;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "isAudioStreamAsDefault failure", ex);
        }

        return isDefault;
    }
}
