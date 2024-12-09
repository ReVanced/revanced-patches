package app.revanced.extension.youtube.patches.theme;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.ThemeHelper;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ThemePatch {
    // color constants used in relation with litho components
    private static final int[] WHITE_VALUES = {
            -1, // comments chip background
            -394759, // music related results panel background
            -83886081, // video chapters list background
    };

    private static final int[] DARK_VALUES = {
            -14145496, // explore drawer background
            -14606047, // comments chip background
            -15198184, // music related results panel background
            -15790321, // comments chip background (new layout)
            -98492127 // video chapters list background
    };

    // Background colors.
    private static final int WHITE_COLOR = Utils.getResourceColor("yt_white1");
    private static final int BLACK_COLOR = Utils.getResourceColor("yt_black1");

    private static final boolean GRADIENT_LOADING_SCREEN_ENABLED = Settings.GRADIENT_LOADING_SCREEN.get();

    /**
     * Injection point.
     *
     * Change the color of Litho components.
     * If the color of the component matches one of the values, return the background color .
     *
     * @param originalValue The original color value.
     * @return The new or original color value
     */
    public static int getValue(int originalValue) {
        if (ThemeHelper.isDarkTheme()) {
            if (anyEquals(originalValue, DARK_VALUES)) return BLACK_COLOR;
        } else {
            if (anyEquals(originalValue, WHITE_VALUES)) return WHITE_COLOR;
        }

        return originalValue;
    }

    private static boolean anyEquals(int value, int... of) {
        for (int v : of) if (value == v) return true;

        return false;
    }

    /**
     * Injection point.
     */
    public static boolean gradientLoadingScreenEnabled(boolean original) {
        return GRADIENT_LOADING_SCREEN_ENABLED;
    }
}
