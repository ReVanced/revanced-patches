package app.revanced.extension.youtube;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;

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

    /**
     * @return The dark theme color as specified by the Theme patch (if included),
     *         or the dark mode background color unpatched YT uses.
     */
    public static int getDarkThemeColor() {
        if (darkThemeColor == null) {
            darkThemeColor = getThemeColor(darkThemeResourceName(), Color.BLACK);
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
            lightThemeColor = getThemeColor(lightThemeResourceName(), Color.WHITE);
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

        return Utils.getColorFromString(colorName);
    }

    /**
     * Sets the system navigation bar color for the activity.
     * Applies the background color obtained from {@link #getBackgroundColor()} to the navigation bar.
     * For Android 10 (API 29) and above, enforces navigation bar contrast to ensure visibility.
     */
    public static void setNavigationBarColor(Activity activity) {
        if (activity == null) {
            Logger.printDebug(() -> "Activity is null, cannot set navigation bar color");
            return;
        }
        Window window = activity.getWindow();
        if (window != null) {
            window.setNavigationBarColor(getBackgroundColor());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.setNavigationBarContrastEnforced(true);
            }
        } else {
            Logger.printDebug(() -> "Failed to get Activity window for navigation bar color");
        }
    }
}
