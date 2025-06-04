package app.revanced.extension.spotify.layout.theme;

import android.graphics.Color;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class CustomThemePatch {

    private static final int BACKGROUND_COLOR = getColorFromString("@color/gray_7");
    private static final int BACKGROUND_COLOR_SECONDARY = getColorFromString("@color/gray_15");
    private static final int ACCENT_COLOR = getColorFromString("@color/spotify_green_157");
    private static final int ACCENT_PRESSED_COLOR =
            getColorFromString("@color/dark_brightaccent_background_press");

    /**
     * Returns an int representation of the color resource or hex code.
     */
    private static int getColorFromString(String colorString) {
        try {
            return Utils.getColorFromString(colorString);
        } catch (Exception ex) {
            Logger.printException(() -> "Invalid color string: " + colorString, ex);
            return Color.BLACK;
        }
    }

    /**
     * Injection point. Returns an int representation of the replaced color from the original color.
     */
    public static int replaceColor(int originalColor) {
        switch (originalColor) {
            // Playlist background color.
            case 0xFF121212:
                return BACKGROUND_COLOR;

            // Share menu background color.
            case 0xFF1F1F1F:
            // Home category pills background color.
            case 0xFF333333:
            // Settings header background color.
            case 0xFF282828:
             // Spotify Connect device list background color.
            case 0xFF2A2A2A:
                return BACKGROUND_COLOR_SECONDARY;

            // Some Lottie animations have a color that's slightly off due to rounding errors.
            case 0xFF1ED760: case 0xFF1ED75F:
            // Intermediate color used in some animations, same rounding issue.
            case 0xFF1DB954: case 0xFF1CB854:
                return ACCENT_COLOR;

            case 0xFF1ABC54:
                return ACCENT_PRESSED_COLOR;

            default:
                return originalColor;
        }
    }
}
