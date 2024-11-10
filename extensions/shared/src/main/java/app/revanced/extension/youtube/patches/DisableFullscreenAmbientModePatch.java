package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

/** @noinspection unused*/
public final class DisableFullscreenAmbientModePatch {

    private static final boolean DISABLE_FULLSCREEN_AMBIENT_MODE = Settings.DISABLE_FULLSCREEN_AMBIENT_MODE.get();

    /**
     * Constant found in: androidx.window.embedding.DividerAttributes
     */
    private static final int DIVIDER_ATTRIBUTES_COLOR_SYSTEM_DEFAULT = -16777216;

    /**
     * Injection point.
     */
    public static int getFullScreenBackgroundColor(int originalColor) {
        if (DISABLE_FULLSCREEN_AMBIENT_MODE) {
            return DIVIDER_ATTRIBUTES_COLOR_SYSTEM_DEFAULT;
        }

        return originalColor;
    }
}
