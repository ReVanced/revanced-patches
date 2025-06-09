package app.revanced.extension.youtube.patches.theme;

import static app.revanced.extension.youtube.patches.theme.ThemePatch.SplashScreenAnimationStyle.styleFromOrdinal;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ThemePatch {

    public enum SplashScreenAnimationStyle {
        DEFAULT(0),
        FPS_60_ONE_SECOND(1),
        FPS_60_TWO_SECOND(2),
        FPS_60_FIVE_SECOND(3),
        FPS_60_BLACK_AND_WHITE(4),
        FPS_30_ONE_SECOND(5),
        FPS_30_TWO_SECOND(6),
        FPS_30_FIVE_SECOND(7),
        FPS_30_BLACK_AND_WHITE(8);
        // There exists a 10th json style used as the switch statement default,
        // but visually it is identical to 60fps one second.

        @Nullable
        static SplashScreenAnimationStyle styleFromOrdinal(int style) {
            // Alternatively can return using values()[style]
            for (SplashScreenAnimationStyle value : values()) {
                if (value.style == style) {
                    return value;
                }
            }

            return null;
        }

        final int style;

        SplashScreenAnimationStyle(int style) {
            this.style = style;
        }
    }

    // color constants used in relation with litho components
    private static final int[] WHITE_VALUES = {
            -1, // comments chip background
            -394759, // music related results panel background
            -83886081, // video chapters list background
    };

    private static final int[] DARK_VALUES = {
            -14145496, // explore drawer background
            -14606047, // comments chip background
            -15198184, // music related results panel background
            -15790321, // comments chip background (new layout)
            -98492127 // video chapters list background
    };

    // Background colors.
    private static final int WHITE_COLOR = Utils.getResourceColor("yt_white1");
    private static final int BLACK_COLOR = Utils.getResourceColor("yt_black1");

    private static final boolean GRADIENT_LOADING_SCREEN_ENABLED = Settings.GRADIENT_LOADING_SCREEN.get();

    /**
     * Injection point.
     * Updates the theme and sets theme's colors.
     */
    @SuppressWarnings("unused")
    public static void setThemeColors() {
        Utils.setThemeLightColor(getThemeColor(lightThemeResourceName(), Color.WHITE));
        Utils.setThemeDarkColor(getThemeColor(darkThemeResourceName(), Color.BLACK));
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

    /**
     * Injection point.
     *
     * Change the color of Litho components.
     * If the color of the component matches one of the values, return the background color .
     *
     * @param originalValue The original color value.
     * @return The new or original color value
     */
    public static int getValue(int originalValue) {
        if (Utils.isDarkModeEnabled()) {
            if (anyEquals(originalValue, DARK_VALUES)) return BLACK_COLOR;
        } else {
            if (anyEquals(originalValue, WHITE_VALUES)) return WHITE_COLOR;
        }

        return originalValue;
    }

    private static boolean anyEquals(int value, int... of) {
        for (int v : of) if (value == v) return true;

        return false;
    }

    /**
     * Injection point.
     */
    public static boolean gradientLoadingScreenEnabled(boolean original) {
        return GRADIENT_LOADING_SCREEN_ENABLED;
    }

    /**
     * Injection point.
     */
    public static int getLoadingScreenType(int original) {
        SplashScreenAnimationStyle style = Settings.SPLASH_SCREEN_ANIMATION_STYLE.get();
        if (style == SplashScreenAnimationStyle.DEFAULT) {
            return original;
        }

        final int replacement = style.style;
        if (original != replacement) {
            Logger.printDebug(() -> "Overriding splash screen style from: "
                    + styleFromOrdinal(original)  + " to: " + style);
        }

        return replacement;
    }
}
