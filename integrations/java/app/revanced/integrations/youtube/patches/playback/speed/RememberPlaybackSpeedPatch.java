package app.revanced.integrations.youtube.patches.playback.speed;

import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

@SuppressWarnings("unused")
public final class RememberPlaybackSpeedPatch {

    /**
     * Injection point.
     */
    public static void newVideoStarted(Object ignoredPlayerController) {
        Logger.printDebug(() -> "newVideoStarted");
        VideoInformation.overridePlaybackSpeed(Settings.PLAYBACK_SPEED_DEFAULT.get());
    }

    /**
     * Injection point.
     * Called when user selects a playback speed.
     *
     * @param playbackSpeed The playback speed the user selected
     */
    public static void userSelectedPlaybackSpeed(float playbackSpeed) {
        if (Settings.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED.get()) {
            Settings.PLAYBACK_SPEED_DEFAULT.save(playbackSpeed);
            Utils.showToastLong("Changed default speed to: " + playbackSpeed + "x");
        }
    }

    /**
     * Injection point.
     * Overrides the video speed.  Called after video loads, and immediately after user selects a different playback speed
     */
    public static float getPlaybackSpeedOverride() {
        return VideoInformation.getPlaybackSpeed();
    }

}
