package app.revanced.integrations.patches.playback.speed;

import android.preference.ListPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public final class RememberPlaybackSpeedPatch {

    /**
     * PreferenceList entries and values, of all available playback speeds.
     */
    private static String[] preferenceListEntries, preferenceListEntryValues;

    @Nullable
    private static String currentVideoId;

    /**
     * Injection point.
     * Called when a new video loads.
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        if (videoId.equals(currentVideoId)) {
            return;
        }
        currentVideoId = videoId;
        VideoInformation.overridePlaybackSpeed(SettingsEnum.PLAYBACK_SPEED_DEFAULT.getFloat());
    }

    /**
     * Injection point.
     * Called when user selects a playback speed.
     *
     * @param playbackSpeed The playback speed the user selected
     */
    public static void userSelectedPlaybackSpeed(float playbackSpeed) {
        if (SettingsEnum.PLAYBACK_SPEED_REMEMBER_LAST_SELECTED.getBoolean()) {
            SettingsEnum.PLAYBACK_SPEED_DEFAULT.saveValue(playbackSpeed);
            ReVancedUtils.showToastLong("Changed default speed to: " + playbackSpeed + "x");
        }
    }

    /**
     * Injection point.
     * Overrides the video speed.  Called after video loads, and immediately after user selects a different playback speed
     */
    public static float getPlaybackSpeedOverride() {
        return VideoInformation.getCurrentPlaybackSpeed();
    }

    /**
     * Initialize a settings preference list.
     *
     * Normally this is done during patching by creating a static xml preference list,
     * but the available playback speeds differ depending if {@link CustomVideoSpeedPatch} is applied or not.
     */
    public static void initializeListPreference(ListPreference preference) {
        if (preferenceListEntries == null) {
            float[] videoSpeeds = CustomVideoSpeedPatch.videoSpeeds;
            preferenceListEntries = new String[videoSpeeds.length];
            preferenceListEntryValues = new String[videoSpeeds.length];
            int i = 0;
            for (float speed : videoSpeeds) {
                String speedString = String.valueOf(speed);
                preferenceListEntries[i] = speedString + "x";
                preferenceListEntryValues[i] = speedString;
                i++;
            }
        }
        preference.setEntries(preferenceListEntries);
        preference.setEntryValues(preferenceListEntryValues);
    }
}
