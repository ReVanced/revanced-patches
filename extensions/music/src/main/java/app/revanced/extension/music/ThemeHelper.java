package app.revanced.extension.music;

import android.app.Activity;
import android.graphics.Color;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

public class ThemeHelper {
    @Nullable
    private static Integer darkThemeColor;
    private static int themeValue;

    /**
     * Injection point.
     */
    @SuppressWarnings("unused")
    public static void setTheme(Enum<?> value) {
        final int newOrdinalValue = value.ordinal();
        if (themeValue != newOrdinalValue) {
            themeValue = newOrdinalValue;
            Logger.printDebug(() -> "Theme value: " + newOrdinalValue);
        }
    }

    public static boolean isDarkTheme() {
        return themeValue == 1;
    }

    public static void setActivityTheme(Activity activity) {
        final var theme = "Theme.Music.Settings.Dark";
        activity.setTheme(Utils.getResourceIdentifier(theme, "style"));
    }

    /**
     * Injection point.
     */
    @SuppressWarnings("SameReturnValue")
    private static String darkThemeResourceName() {
        // Value is changed by Theme patch, if included.
        return "@color/yt_black3";
    }

    /**
     * @return The dark theme color as specified by the Theme patch (if included),
     *         or the dark mode background color unpatched YT Music uses.
     */
    public static int getDarkThemeColor() {
        if (darkThemeColor == null) {
            darkThemeColor = getColorInt(darkThemeResourceName());
        }
        return darkThemeColor;
    }

    private static int getColorInt(String colorString) {
        if (colorString.startsWith("#")) {
            return Color.parseColor(colorString);
        }
        return Utils.getResourceColor(colorString);
    }

    public static int getBackgroundColor() {
        return getDarkThemeColor();
    }

    public static int getForegroundColor() {
        // Since we are not using a light theme, return a contrasting color.
        return Color.WHITE;
    }
}
