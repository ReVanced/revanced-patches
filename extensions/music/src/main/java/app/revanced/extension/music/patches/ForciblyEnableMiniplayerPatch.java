package app.revanced.extension.music.patches;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class ForciblyEnableMiniplayerPatch {
    /**
     * Injection point
     */
    public static boolean enableForcedMiniplayerPatch(boolean original) {
        return Settings.FORCIBLY_ENABLE_MINIPLAYER.get() || original;
    }
}