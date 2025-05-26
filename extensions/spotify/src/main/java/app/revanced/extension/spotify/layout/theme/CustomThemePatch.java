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
        return color == 0xff1ed760L ? ACCENT : color;
    }

    public static int replaceAccentColor(int color) {
        return color == 0xff1ed760 || color == 0xff1ed75f // This off-by-one color appears in some lottie animations
                ? (int)ACCENT
                : color;
    }

    public static long replaceAccentPressedColor(long color) {
        return color == 0xff1abc54L ? ACCENT_PRESSED : color;
    }
}
