package app.revanced.extension.shared.ui;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Utility class for converting design units (dp) and screen percentages to pixels.
 */
public final class Dim {
    private Dim() {} // Prevent instantiation.

    private static final DisplayMetrics METRICS = Resources.getSystem().getDisplayMetrics();
    public static final int SCREEN_WIDTH = METRICS.widthPixels;
    public static final int SCREEN_HEIGHT = METRICS.heightPixels;

    // DP constants (density-independent pixels).
    public static final int dp1  = dp(1);
    public static final int dp2  = dp(2);
    public static final int dp4  = dp(4);
    public static final int dp6  = dp(6);
    public static final int dp7  = dp(7);
    public static final int dp8  = dp(8);
    public static final int dp10 = dp(10);
    public static final int dp12 = dp(12);
    public static final int dp16 = dp(16);
    public static final int dp20 = dp(20);
    public static final int dp24 = dp(24);
    public static final int dp28 = dp(28);
    public static final int dp32 = dp(32);
    public static final int dp36 = dp(36);
    public static final int dp40 = dp(40);
    public static final int dp48 = dp(48);

    /**
     * Converts dp (density-independent pixels) to actual device pixels.
     * Uses Android's official TypedValue.applyDimension() for accurate rounding.
     *
     * @param dp The dp value to convert (supports float, e.g. 1.2f).
     * @return The equivalent pixel value as int.
     */
    public static int dp(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, METRICS);
    }

    /**
     * Converts a percentage of the screen height to pixels.
     *
     * @param percent The percentage (0–100).
     * @return The pixel value corresponding to the percentage of screen height.
     */
    public static int pctHeight(int percent) {
        return (SCREEN_HEIGHT * percent) / 100;
    }

    /**
     * Converts a percentage of the screen width to pixels.
     *
     * @param percent The percentage (0–100).
     * @return The pixel value corresponding to the percentage of screen width.
     */
    public static int pctWidth(int percent) {
        return (SCREEN_WIDTH * percent) / 100;
    }

    /**
     * Converts a percentage of the screen's portrait width (min side) to pixels.
     *
     * @param percent The percentage (0–100).
     * @return The pixel value.
     */
    public static int pctPortraitWidth(int percent) {
        final int portraitWidth = Math.min(SCREEN_WIDTH, SCREEN_HEIGHT);
        return (int) (portraitWidth * (percent / 100.0f));
    }

    /**
     * Creates an array of corner radii for a rounded rectangle.
     * All corners use the same radius.
     *
     * @param dp radius in density-independent pixels
     * @return array of 8 floats: [top-left-x, top-left-y, top-right-x, top-right-y, ...]
     */
    public static float[] roundedCorners(float dp) {
        final float r = dp(dp);
        return new float[]{r, r, r, r, r, r, r, r};
    }
}