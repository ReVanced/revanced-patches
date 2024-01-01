package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

/** @noinspection unused*/
public class DisableResumingStartupShortsPlayerPatch {

    /**
     * Injection point.
     */
    public static boolean disableResumingStartupShortsPlayer() {
        return Settings.DISABLE_RESUMING_SHORTS_PLAYER.get();
    }
}
