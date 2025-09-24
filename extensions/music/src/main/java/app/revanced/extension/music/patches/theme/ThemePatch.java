package app.revanced.extension.music.patches.theme;

import app.revanced.extension.shared.theme.BaseThemePatch;

@SuppressWarnings("unused")
public class ThemePatch extends BaseThemePatch {

    // Color constants used in relation with litho components.
    private static final int[] DARK_VALUES = {
            0xFF212121, // Comments box background.
            0xFF030303, // Button container background in album.
            0xFF000000, // Button container background in playlist.
    };

    /**
     * Injection point.
     * <p>
     * Change the color of Litho components.
     * If the color of the component matches one of the values, return the background color.
     *
     * @param originalValue The original color value.
     * @return The new or original color value.
     */
    public static int getValue(int originalValue) {
        return processColorValue(originalValue, DARK_VALUES, null);
    }
}
