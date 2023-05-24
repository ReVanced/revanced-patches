package app.revanced.integrations.patches.playback.speed;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public final class RememberPlaybackSpeedPatch {

    /**
     * Injection point.
     */
    public static void newVideoStarted(Object ignoredPlayerController) {
        LogHelper.printDebug(() -> "newVideoStarted");
        VideoInformation.overridePlaybackSpeed(SettingsEnum.PLAYBACK_SPEED_DEFAULT.getFloat());
    }

    /**
     * Injection point.
     * Called when user selects a playback speed.
     *
     * @param playbackSpeed The playback speed the user selected
     */
    public static void userSelectedPlaybackSpeed(float playbackSpeed) {
        if (SettingsEnum.REMEMBER_PLAYBACK_SPEED_LAST_SELECTED.getBoolean()) {
            SettingsEnum.PLAYBACK_SPEED_DEFAULT.saveValue(playbackSpeed);
            ReVancedUtils.showToastLong("Changed default speed to: " + playbackSpeed + "x");
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
