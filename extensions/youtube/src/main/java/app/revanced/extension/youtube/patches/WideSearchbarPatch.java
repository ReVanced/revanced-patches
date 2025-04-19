package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class WideSearchbarPatch {

    private static final Boolean WIDE_SEARCHBAR_ENABLED = Settings.WIDE_SEARCHBAR.get();

    /**
     * Injection point.
=    */
    public static boolean enableWideSearchbar(boolean original) {
        return WIDE_SEARCHBAR_ENABLED || original;
    }
}
