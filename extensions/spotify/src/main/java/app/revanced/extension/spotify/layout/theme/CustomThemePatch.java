package app.revanced.extension.spotify.layout.theme;

import android.graphics.Color;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class CustomThemePatch {

    private static final long ACCENT = getThemeColor("@color/spotify_green_157");
    private static final long ACCENT_PRESSED = getThemeColor("@color/dark_brightaccent_background_press");

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

    public static long replaceAccentColor(long color) {
        // Some lottie animations have a color that's slightly off due to rounding errors
        return color == 0xff1ed760L || color == 0xff1ed75fL
                // Intermediate color used in some animations, same rounding issue
                || color == 0xff1db954L || color == 0xff1cb854L
                ? ACCENT
                : color;
    }

    public static int replaceAccentColor(int color) {
        return (int)replaceAccentColor(Integer.toUnsignedLong(color));
    }

    public static long replaceAccentPressedColor(long color) {
        return color == 0xff1abc54L ? ACCENT_PRESSED : color;
    }
}
