package app.revanced.extension.youtube;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

public class ThemeHelper {
    @ColorInt
    @Nullable
    private static Integer darkThemeColor, lightThemeColor;

    private static int themeValue;

    /**
     * Injection point.
     * Updates the theme and sets theme's colors.
     */
    @SuppressWarnings("unused")
    public static void setTheme(Enum<?> value) {
        final int newOrdinalValue = value.ordinal();
        if (themeValue != newOrdinalValue) {
            themeValue = newOrdinalValue;
            Logger.printDebug(() -> "Theme value: " + newOrdinalValue);

            Utils.setThemeDarkColor(getDarkThemeColor());
            Utils.setThemeLightColor(getLightThemeColor());
        }
    }

    private static boolean isDarkTheme() {
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
    private static String lightThemeResourceName() {
        // Value is changed by Theme patch, if included.
        return "@color/yt_white1";
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
    @ColorInt
    private static int getDarkThemeColor() {
        if (darkThemeColor == null) {
            darkThemeColor = getThemeColor(darkThemeResourceName(), Color.BLACK);
        }
        return darkThemeColor;
    }

    /**
     * @return The light theme color as specified by the Theme patch (if included),
     *         or the non dark mode background color unpatched YT uses.
     */
    @ColorInt
    private static int getLightThemeColor() {
        if (lightThemeColor == null) {
            lightThemeColor = getThemeColor(lightThemeResourceName(), Color.WHITE);
        }
        return lightThemeColor;
    }


    @ColorInt
    private static int getThemeColor(String resourceName, int defaultColor) {
        try {
            return Utils.getColorFromString(resourceName);
        } catch (Exception ex) {
            // User entered an invalid custom theme color.
            // Normally this should never be reached, and no localized strings are needed.
            Utils.showToastLong("Invalid custom theme color: " + resourceName);
            return defaultColor;
        }
    }

    public static int getToolbarBackgroundColor() {
        final String colorName = isDarkTheme()
                ? "yt_black3"
                : "yt_white1";

        return Utils.getColorFromString(colorName);
    }

    /**
     * Sets the system navigation bar color for the activity.
     * Applies the background color obtained from {@link Utils#getAppBackgroundColor()} to the navigation bar.
     * For Android 10 (API 29) and above, enforces navigation bar contrast to ensure visibility.
     */
    public static void setNavigationBarColor(@Nullable Window window) {
        if (window == null) {
            Logger.printDebug(() -> "Cannot set navigation bar color, window is null");
            return;
        }

        window.setNavigationBarColor(Utils.getAppBackgroundColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(true);
        }
    }
}
