package app.revanced.integrations.youtube.patches.theme;

import android.graphics.Color;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

@SuppressWarnings("unused")
public final class SeekbarColorPatch {

    private static final boolean USE_SEEKBAR_CUSTOM_COLOR = Settings.SEEKBAR_CUSTOM_COLOR.get();

    /**
     * Default color of the seekbar.
     */
    private static final int ORIGINAL_SEEKBAR_COLOR = 0xFFFF0000;

    /**
     * Default YouTube seekbar color brightness.
     */
    private static final float ORIGINAL_SEEKBAR_COLOR_BRIGHTNESS;

    /**
     * If {@link Settings#SEEKBAR_CUSTOM_COLOR} is enabled,
     * this is the color value of {@link Settings#SEEKBAR_CUSTOM_COLOR_VALUE}.
     * Otherwise this is {@link #ORIGINAL_SEEKBAR_COLOR}.
     */
    private static int seekbarColor = ORIGINAL_SEEKBAR_COLOR;

    /**
     * Custom seekbar hue, saturation, and brightness values.
     */
    private static final float[] customSeekbarColorHSV = new float[3];

    static {
        float[] hsv = new float[3];
        Color.colorToHSV(ORIGINAL_SEEKBAR_COLOR, hsv);
        ORIGINAL_SEEKBAR_COLOR_BRIGHTNESS = hsv[2];

        if (USE_SEEKBAR_CUSTOM_COLOR) {
            loadCustomSeekbarColor();
        }
    }

    private static void loadCustomSeekbarColor() {
        try {
            seekbarColor = Color.parseColor(Settings.SEEKBAR_CUSTOM_COLOR_VALUE.get());
            Color.colorToHSV(seekbarColor, customSeekbarColorHSV);
        } catch (Exception ex) {
            Utils.showToastShort("Invalid seekbar color value. Using default value.");
            Settings.SEEKBAR_CUSTOM_COLOR_VALUE.resetToDefault();
            loadCustomSeekbarColor();
        }
    }

    public static int getSeekbarColor() {
        return seekbarColor;
    }


    /**
     * Injection point.
     *
     * Overrides all Litho components that use the YouTube seekbar color.
     * Used only for the video thumbnails seekbar.
     *
     * If {@link Settings#HIDE_SEEKBAR_THUMBNAIL} is enabled, this returns a fully transparent color.
     */
    public static int getLithoColor(int colorValue) {
        if (colorValue == ORIGINAL_SEEKBAR_COLOR) {
            if (Settings.HIDE_SEEKBAR_THUMBNAIL.get()) {
                return 0x00000000;
            }
            return getSeekbarColorValue(ORIGINAL_SEEKBAR_COLOR);
        }
        return colorValue;
    }

    /**
     * Injection point.
     *
     * Overrides color when video player seekbar is clicked.
     */
    public static int getVideoPlayerSeekbarClickedColor(int colorValue) {
        return colorValue == ORIGINAL_SEEKBAR_COLOR
                ? getSeekbarColorValue(ORIGINAL_SEEKBAR_COLOR)
                : colorValue;
    }

    /**
     * Injection point.
     *
     * Overrides color used for the video player seekbar.
     */
    public static int getVideoPlayerSeekbarColor(int originalColor) {
        return getSeekbarColorValue(originalColor);
    }

    /**
     * Color parameter is changed to the custom seekbar color, while retaining
     * the brightness and alpha changes of the parameter value compared to the original seekbar color.
     */
    private static int getSeekbarColorValue(int originalColor) {
        try {
            if (!USE_SEEKBAR_CUSTOM_COLOR || originalColor == seekbarColor) {
                return originalColor; // nothing to do
            }
            final int alphaDifference = Color.alpha(originalColor) - Color.alpha(ORIGINAL_SEEKBAR_COLOR);

            // The seekbar uses the same color but different brightness for different situations.
            float[] hsv = new float[3];
            Color.colorToHSV(originalColor, hsv);
            final float brightnessDifference = hsv[2] - ORIGINAL_SEEKBAR_COLOR_BRIGHTNESS;

            // Apply the brightness difference to the custom seekbar color.
            hsv[0] = customSeekbarColorHSV[0];
            hsv[1] = customSeekbarColorHSV[1];
            hsv[2] = clamp(customSeekbarColorHSV[2] + brightnessDifference, 0, 1);

            final int replacementAlpha = clamp(Color.alpha(seekbarColor) + alphaDifference, 0, 255);
            final int replacementColor = Color.HSVToColor(replacementAlpha, hsv);
            Logger.printDebug(() -> String.format("Original color: #%08X  replacement color: #%08X",
                            originalColor, replacementColor));
            return replacementColor;
        } catch (Exception ex) {
            Logger.printException(() -> "getSeekbarColorValue failure", ex);
            return originalColor;
        }
    }

    static int clamp(int value, int lower, int upper) {
        return Math.max(lower, Math.min(value, upper));
    }

    static float clamp(float value, float lower, float upper) {
        return Math.max(lower, Math.min(value, upper));
    }
}
