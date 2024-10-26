package app.revanced.extension.youtube.patches.playback.speed;

import static app.revanced.extension.shared.StringRef.str;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class RememberPlaybackSpeedPatch {

    private static final long TOAST_DELAY_MILLISECONDS = 750;

    private static long lastTimeSpeedChanged;

    /**
     * Injection point.
     */
    public static void newVideoStarted(VideoInformation.PlaybackController ignoredPlayerController) {
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

            // Prevent toast spamming if using the 0.05x adjustments.
            // Show exactly one toast after the user stops interacting with the speed menu.
            final long now = System.currentTimeMillis();
            lastTimeSpeedChanged = now;

            Utils.runOnMainThreadDelayed(() -> {
                if (lastTimeSpeedChanged == now) {
                    Utils.showToastLong(str("revanced_remember_playback_speed_toast", (playbackSpeed + "x")));
                } // else, the user made additional speed adjustments and this call is outdated.
            }, TOAST_DELAY_MILLISECONDS);
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
