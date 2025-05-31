package app.revanced.extension.youtube;

import static app.revanced.extension.shared.Utils.clamp;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.text.style.ReplacementSpan;
import android.text.TextPaint;
import android.view.Window;

import androidx.annotation.NonNull;
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

    public static int getDialogBackgroundColor() {
        final String colorName = isDarkTheme()
                ? "yt_black1"
                : "yt_white1";

        return Utils.getColorFromString(colorName);
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
    public static void setNavigationBarColor(@Nullable Window window) {
        if (window == null) {
            Logger.printDebug(() -> "Cannot set navigation bar color, window is null");
            return;
        }

        window.setNavigationBarColor(getBackgroundColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(true);
        }
    }

    /**
     * Adjusts the brightness of a color by lightening or darkening it based on the given factor.
     * <p>
     * If the factor is greater than 1, the color is lightened by interpolating toward white (#FFFFFF).
     * If the factor is less than or equal to 1, the color is darkened by scaling its RGB components toward black (#000000).
     * The alpha channel remains unchanged.
     *
     * @param color  The input color to adjust, in ARGB format.
     * @param factor The adjustment factor. Use values > 1.0f to lighten (e.g., 1.11f for slight lightening)
     *               or values <= 1.0f to darken (e.g., 0.95f for slight darkening).
     * @return The adjusted color in ARGB format.
     */
    public static int adjustColorBrightness(int color, float factor) {
        final int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        if (factor > 1.0f) {
            // Lighten: Interpolate toward white (255)
            final float t = 1.0f - (1.0f / factor); // Interpolation parameter
            red = Math.round(red + (255 - red) * t);
            green = Math.round(green + (255 - green) * t);
            blue = Math.round(blue + (255 - blue) * t);
        } else {
            // Darken or no change: Scale toward black
            red = (int) (red * factor);
            green = (int) (green * factor);
            blue = (int) (blue * factor);
        }

        // Ensure values are within [0, 255]
        red = clamp(red, 0, 255);
        green = clamp(green, 0, 255);
        blue = clamp(blue, 0, 255);

        return Color.argb(alpha, red, green, blue);
    }
}
