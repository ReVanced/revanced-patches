package app.revanced.integrations.youtube;

import android.app.Activity;
import android.graphics.Color;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

public class ThemeHelper {
    @Nullable
    private static Integer darkThemeColor, lightThemeColor;
    private static int themeValue;

    /**
     * Injection point.
     */
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
    private static String darkThemeResourceName() {
        // Value is changed by Theme patch, if included.
        return "@android:color/black";
    }

    /**
     * @return The dark theme color as specified by the Theme patch (if included),
     *         or the Android color of black.
     */
    public static int getDarkThemeColor() {
        if (darkThemeColor == null) {
            darkThemeColor = getColorInt(darkThemeResourceName());
        }
        return darkThemeColor;
    }

    /**
     * Injection point.
     */
    private static String lightThemeResourceName() {
        // Value is changed by Theme patch, if included.
        return "@android:color/white";
    }

    /**
     * @return The light theme color as specified by the Theme patch (if included),
     *         or the Android color of white.
     */
    public static int getLightThemeColor() {
        if (lightThemeColor == null) {
            lightThemeColor = getColorInt(lightThemeResourceName());
        }
        return lightThemeColor;
    }

    private static int getColorInt(String colorString) {
        if (colorString.startsWith("#")) {
            return Color.parseColor(colorString);
        }
        return Utils.getResourceColor(colorString);
    }
}
