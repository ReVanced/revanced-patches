package app.revanced.extension.spotify.layout.theme;

import android.graphics.Color;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class CustomThemePatch {

    private static final int ACCENT = (int)getThemeColor("@color/spotify_green_157");
    private static final int ACCENT_PRESSED = (int)getThemeColor("@color/dark_brightaccent_background_press");

    /**
     * Injection point.
     */
    public static long getThemeColor(String colorString) {
        try {
            return Utils.getColorFromString(colorString);
        } catch (Exception ex) {
            Logger.printException(() -> "Invalid custom color: " + colorString, ex);
            return Color.BLACK;
        }
    }

    public static long replaceColor(long color) {
        return replaceColor((int)color);
    }

    public static int replaceColor(int color) {
        switch (color) {
            case 0xff1ed760: case 0xff1ed75f: // Some lottie animations have a color that's slightly off due to rounding errors
            case 0xff1db954: case 0xff1cb854: // Intermediate color used in some animations, same rounding issue
                return ACCENT;

            case 0xff1abc54:
                return ACCENT_PRESSED;

            default:
                return color;
        }
    }
}
