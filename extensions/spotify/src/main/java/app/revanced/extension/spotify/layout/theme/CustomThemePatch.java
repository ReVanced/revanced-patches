package app.revanced.extension.spotify.layout.theme;

import android.graphics.Color;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class CustomThemePatch {

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
        if (color == 0xff1ed760L)
            return getThemeColor("@color/spotify_green_157");
        return color;
    }

    public static int replaceAccentColor(int color) {
        if (color == 0xff1ed760 || color == 0xff1ed75f) // This off-by-one color appears in some lottie animations
            return (int)getThemeColor("@color/spotify_green_157");
        return color;
    }

    public static long replaceAccentPressedColor(long color) {
        if (color == 0xff1abc54L)
            return getThemeColor("@color/dark_brightaccent_background_press");
        return color;
    }
}
