package app.revanced.integrations.patches.playback.speed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class RememberPlaybackSpeedPatch {

    /**
     * The current playback speed
     */
    private static float currentPlaybackSpeed = getLastRememberedPlaybackSpeed();

    private final static float DEFAULT_PLAYBACK_SPEED = (float) SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE.getDefaultValue();

    @Nullable
    private static String currentVideoId;

    private static float getLastRememberedPlaybackSpeed() {
        return SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE.getFloat();
    }

    private static void rememberPlaybackSpeed() {
        SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED_VALUE.saveValue(currentPlaybackSpeed);
    }

    private static boolean rememberLastSelectedPlaybackSpeed() {
        return SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED.getBoolean();
    }

    /**
     * Injection point.
     * Called when a new video loads.
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        if (videoId.equals(currentVideoId)) {
            return;
        }

        currentVideoId = videoId;
        currentPlaybackSpeed = getLastRememberedPlaybackSpeed();
    }

    /**
     * Injection point.
     * Called when a playback speed is selected.
     *
     * @param playbackSpeed The playback speed to set.
     */
    public static void setPlaybackSpeed(final float playbackSpeed) {
        LogHelper.printDebug(() -> "Playback speed changed to: " + playbackSpeed);

        currentPlaybackSpeed = playbackSpeed;

        if (rememberLastSelectedPlaybackSpeed()) {
            rememberPlaybackSpeed();

            ReVancedUtils.showToastLong("Remembering playback speed: " + playbackSpeed + "x");
        } else {
            if (getLastRememberedPlaybackSpeed() == DEFAULT_PLAYBACK_SPEED) return;

            ReVancedUtils.showToastLong("Applying playback speed: " + playbackSpeed + "x");
        }
    }

    /**
     * Injection point.
     * Called when playback first starts,
     * and also called immediately after the user selects a new video speed.
     *
     * @return The currently set playback speed.
     */
    public static float getCurrentPlaybackSpeed() {
        return currentPlaybackSpeed;
    }
}
