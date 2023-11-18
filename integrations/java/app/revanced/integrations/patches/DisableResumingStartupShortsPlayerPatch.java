package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

/** @noinspection unused*/
public class DisableResumingStartupShortsPlayerPatch {

    /**
     * Injection point.
     */
    public static boolean disableResumingStartupShortsPlayer() {
        return SettingsEnum.DISABLE_RESUMING_SHORTS_PLAYER.getBoolean();
    }
}
