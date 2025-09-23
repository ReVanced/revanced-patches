package app.revanced.extension.music.patches.theme;

import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public class ThemePatch {
    // Color constants used in relation with litho components.
    private static final int[] DARK_VALUES = {
            0xFF212121, // Comments box background.
            0xFF030303, // Button container background in album.
            0xFF000000, // Button container background in playlist.
    };

    // Background colors
    private static final int BLACK_COLOR = Utils.getResourceColor("yt_black1");

     /**
     * Injection point.
     * <p>
     * Change the color of Litho components.
     * If the color of the component matches one of the values, return the background color .
     *
     * @param originalValue The original color value.
     * @return The new or original color value
     */
    public static int getValue(int originalValue) {
        if (Utils.isDarkModeEnabled()) {
            if (anyEquals(originalValue, DARK_VALUES)) return BLACK_COLOR;
        }

        return originalValue;
    }

    private static boolean anyEquals(int value, int... of) {
        for (int v : of) if (value == v) return true;

        return false;
    }
}
