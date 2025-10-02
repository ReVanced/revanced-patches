package app.revanced.extension.shared.theme;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public abstract class BaseThemePatch {
    // Background colors.
    protected static final int BLACK_COLOR = Utils.getResourceColor("yt_black1");
    protected static final int WHITE_COLOR = Utils.getResourceColor("yt_white1");

    /**
     * Check if a value matches any of the provided values.
     *
     * @param value The value to check.
     * @param of    The array of values to compare against.
     * @return True if the value matches any of the provided values.
     */
    protected static boolean anyEquals(int value, int... of) {
        for (int v : of) {
            if (value == v) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to process color values for Litho components.
     *
     * @param originalValue The original color value.
     * @param darkValues    Array of dark mode color values to match.
     * @param lightValues   Array of light mode color values to match.
     * @return The new or original color value.
     */
    protected static int processColorValue(int originalValue, int[] darkValues, @Nullable int[] lightValues) {
        if (Utils.isDarkModeEnabled()) {
            if (anyEquals(originalValue, darkValues)) {
                return BLACK_COLOR;
            }
        } else if (lightValues != null && anyEquals(originalValue, lightValues)) {
            return WHITE_COLOR;
        }

        return originalValue;
    }
}
