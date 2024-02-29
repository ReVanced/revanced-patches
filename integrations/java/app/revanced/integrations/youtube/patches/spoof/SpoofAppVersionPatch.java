package app.revanced.integrations.youtube.patches.spoof;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class SpoofAppVersionPatch {

    private static final boolean SPOOF_APP_VERSION_ENABLED;
    private static final String SPOOF_APP_VERSION_TARGET;

    static {
        // TODO: remove this migration code
        // Spoof targets 16.x and 17.x that no longer reliably work.
        if (Settings.SPOOF_APP_VERSION_TARGET.get().compareTo("18.01.01") < 0) {
            Logger.printInfo(() -> "Resetting spoof app version target");
            Settings.SPOOF_APP_VERSION_TARGET.resetToDefault();
        }
        // End migration

        SPOOF_APP_VERSION_ENABLED = Settings.SPOOF_APP_VERSION.get();
        SPOOF_APP_VERSION_TARGET = Settings.SPOOF_APP_VERSION_TARGET.get();
    }

    /**
     * Injection point
     */
    public static String getYouTubeVersionOverride(String version) {
        if (SPOOF_APP_VERSION_ENABLED) return SPOOF_APP_VERSION_TARGET;
        return version;
    }

    public static boolean isSpoofingToEqualOrLessThan(String version) {
        return SPOOF_APP_VERSION_ENABLED && SPOOF_APP_VERSION_TARGET.compareTo(version) <= 0;
    }

}
