package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableResumingShortsOnStartupPatch {

    /**
     * Injection point.
     */
    public static boolean disableResumingShortsOnStartup() {
        return Settings.DISABLE_RESUMING_SHORTS_ON_STARTUP.get();
    }

    /**
     * Injection point.
     */
    public static boolean disableResumingShortsOnStartup(boolean original) {
        return original && !Settings.DISABLE_RESUMING_SHORTS_ON_STARTUP.get();
    }
}
