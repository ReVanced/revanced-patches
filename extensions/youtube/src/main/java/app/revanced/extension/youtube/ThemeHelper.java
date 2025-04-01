package app.revanced.extension.youtube;

import android.app.Activity;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

public class ThemeHelper {
    @Nullable
    private static Integer darkThemeColor, lightThemeColor;
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
        final var theme = isDarkTheme()
                ? "Theme.YouTube.Settings.Dark"
                : "Theme.YouTube.Settings";
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
     *         or the dark mode background color unpatched YT uses.
     */
    public static int getDarkThemeColor() {
        if (darkThemeColor == null) {
            darkThemeColor = Utils.getColorInt(darkThemeResourceName());
        }
        return darkThemeColor;
    }

    /**
     * Injection point.
     */
    @SuppressWarnings("SameReturnValue")
    private static String lightThemeResourceName() {
        // Value is changed by Theme patch, if included.
        return "@color/yt_white1";
    }

    /**
     * @return The light theme color as specified by the Theme patch (if included),
     *         or the non dark mode background color unpatched YT uses.
     */
    public static int getLightThemeColor() {
        if (lightThemeColor == null) {
            lightThemeColor = Utils.getColorInt(lightThemeResourceName());
        }
        return lightThemeColor;
    }

    public static int getBackgroundColor() {
        return isDarkTheme() ? getDarkThemeColor() : getLightThemeColor();
    }

    public static int getForegroundColor() {
        return isDarkTheme() ? getLightThemeColor() : getDarkThemeColor();
    }

    public static int getToolbarBackgroundColor() {
        final String colorName = isDarkTheme()
                ? "yt_black3"
                : "yt_white1";

        return Utils.getColorInt(colorName);
    }
}
