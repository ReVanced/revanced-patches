package app.revanced.extension.youtube.patches.theme;

import static app.revanced.extension.shared.StringRef.str;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;

import java.util.Arrays;
import java.util.Locale;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class SeekbarColorPatch {

    private static final boolean SEEKBAR_CUSTOM_COLOR_ENABLED = Settings.SEEKBAR_CUSTOM_COLOR.get();

    private static final boolean HIDE_SEEKBAR_THUMBNAIL_ENABLED = Settings.HIDE_SEEKBAR_THUMBNAIL.get();

    /**
     * Default color of the litho seekbar.
     * Differs slightly from the default custom seekbar color setting.
     */
    private static final int ORIGINAL_SEEKBAR_COLOR = 0xFFFF0000;

    /**
     * Feed default colors of the gradient seekbar.
     */
    private static final int[] FEED_ORIGINAL_SEEKBAR_GRADIENT_COLORS = { 0xFFFF0033, 0xFFFF2791 };

    /**
     * Feed default positions of the gradient seekbar.
     */
    private static final float[] FEED_ORIGINAL_SEEKBAR_GRADIENT_POSITIONS = { 0.8f, 1.0f };

    /**
     * Empty seekbar gradient, if hide seekbar in feed is enabled.
     */
    private static final int[] HIDDEN_SEEKBAR_GRADIENT_COLORS = { 0x0, 0x0 };

    /**
     * Default YouTube seekbar color brightness.
     */
    private static final float ORIGINAL_SEEKBAR_COLOR_BRIGHTNESS;

    /**
     * If {@link Settings#SEEKBAR_CUSTOM_COLOR} is enabled,
     * this is the color value of {@link Settings#SEEKBAR_CUSTOM_COLOR_PRIMARY}.
     * Otherwise this is {@link #ORIGINAL_SEEKBAR_COLOR}.
     */
    private static int seekbarColor = ORIGINAL_SEEKBAR_COLOR;

    /**
     * Custom seekbar hue, saturation, and brightness values.
     */
    private static final float[] customSeekbarColorHSV = new float[3];

    /**
     * Custom seekbar color, used for linear gradient replacements.
     */
    private static final int[] customSeekbarColorGradient = new int[2];

    static {
        float[] hsv = new float[3];
        Color.colorToHSV(ORIGINAL_SEEKBAR_COLOR, hsv);
        ORIGINAL_SEEKBAR_COLOR_BRIGHTNESS = hsv[2];

        if (SEEKBAR_CUSTOM_COLOR_ENABLED) {
            loadCustomSeekbarColor();
        }
    }

    private static void loadCustomSeekbarColor() {
        try {
            seekbarColor = Color.parseColor(Settings.SEEKBAR_CUSTOM_COLOR_PRIMARY.get());
            Color.colorToHSV(seekbarColor, customSeekbarColorHSV);

            customSeekbarColorGradient[0] = seekbarColor;
            customSeekbarColorGradient[1] = Color.parseColor(
                    Settings.SEEKBAR_CUSTOM_COLOR_ACCENT.get());
        } catch (Exception ex) {
            Utils.showToastShort(str("revanced_seekbar_custom_color_invalid"));
            Settings.SEEKBAR_CUSTOM_COLOR_PRIMARY.resetToDefault();
            Settings.SEEKBAR_CUSTOM_COLOR_ACCENT.resetToDefault();

            loadCustomSeekbarColor();
        }
    }

    public static int getSeekbarColor() {
        return seekbarColor;
    }

    /**
     * Injection point
     */
    public static boolean useLotteLaunchSplashScreen(boolean original) {
        Logger.printDebug(() -> "useLotteLaunchSplashScreen original: " + original);

        if (SEEKBAR_CUSTOM_COLOR_ENABLED) return false;

        return original;
    }

    private static int colorChannelTo3Bits(int channel8Bits) {
        final float channel3Bits = channel8Bits * 7 / 255f;

        // If a color channel is near zero, then allow rounding up so values between
        // 0x12 and 0x23 will show as 0x24. But always round down when the channel is
        // near full saturation, otherwise rounding to nearest will cause all values
        // between 0xEC and 0xFE to always show as full saturation (0xFF).
        return channel3Bits < 6
                ? Math.round(channel3Bits)
                : (int) channel3Bits;
    }

    private static String get9BitStyleIdentifier(int color24Bit) {
        final int r3 = colorChannelTo3Bits(Color.red(color24Bit));
        final int g3 = colorChannelTo3Bits(Color.green(color24Bit));
        final int b3 = colorChannelTo3Bits(Color.blue(color24Bit));

        return String.format(Locale.US, "splash_seekbar_color_style_%d_%d_%d", r3, g3, b3);
    }

    /**
     * Injection point
     */
    public static void setSplashAnimationDrawableTheme(AnimatedVectorDrawable vectorDrawable) {
        // Alternatively a ColorMatrixColorFilter can be used to change the color of the drawable
        // without using any styles, but a color filter cannot selectively change the seekbar
        // while keeping the red YT logo untouched.
        // Even if the seekbar color xml value is changed to a completely different color (such as green),
        // a color filter still cannot be selectively applied when the drawable has more than 1 color.
        try {
            String seekbarStyle = get9BitStyleIdentifier(seekbarColor);
            Logger.printDebug(() -> "Using splash seekbar style: " + seekbarStyle);

            final int styleIdentifierDefault = Utils.getResourceIdentifier(
                    seekbarStyle,
                    "style"
            );
            if (styleIdentifierDefault == 0) {
                throw new RuntimeException("Seekbar style not found: " + seekbarStyle);
            }

            Resources.Theme theme = Utils.getContext().getResources().newTheme();
            theme.applyStyle(styleIdentifierDefault, true);

            vectorDrawable.applyTheme(theme);
        } catch (Exception ex) {
            Logger.printException(() -> "setSplashAnimationDrawableTheme failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static boolean showWatchHistoryProgressDrawable(boolean original) {
        return !HIDE_SEEKBAR_THUMBNAIL_ENABLED && original;
    }

    /**
     * Injection point.
     */
    public static int getSeekbarScrubHandleColor(int colorValue) {
        return SEEKBAR_CUSTOM_COLOR_ENABLED
                ? seekbarColor
                : colorValue;
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
        /* FIXME */ if (colorValue == 0xFFFF0033) { return seekbarColor; }

        if (colorValue == ORIGINAL_SEEKBAR_COLOR) {
            if (HIDE_SEEKBAR_THUMBNAIL_ENABLED) {
                return 0x0;
            }

            return seekbarColor;
        }

        return colorValue;
    }

    /**
     * Injection point.
     */
    public static int[] getLinearGradient(int[] original) {
        return SEEKBAR_CUSTOM_COLOR_ENABLED
                ? customSeekbarColorGradient
                : original;
    }

    private static String colorArrayToHex(int[] colors) {
        final int length = colors.length;
        StringBuilder builder = new StringBuilder(length * 12);
        builder.append("[");

        int i = 0;
        for (int color : colors) {
            builder.append(String.format("#%X", color));
            if (++i < length) {
                builder.append(", ");
            }
        }

        builder.append("]");
        return builder.toString();
    }

    /**
     * Injection point.
     */
    public static int[] setLinearGradient(int[] colors, float[] positions) {
        if (SEEKBAR_CUSTOM_COLOR_ENABLED || HIDE_SEEKBAR_THUMBNAIL_ENABLED) {
            // Most litho usage of linear gradients is hooked here,
            // so must only change if the values are those for the seekbar.
            if ((Arrays.equals(FEED_ORIGINAL_SEEKBAR_GRADIENT_COLORS, colors)
                    && Arrays.equals(FEED_ORIGINAL_SEEKBAR_GRADIENT_POSITIONS, positions))) {
                return HIDE_SEEKBAR_THUMBNAIL_ENABLED
                        ? HIDDEN_SEEKBAR_GRADIENT_COLORS
                        : customSeekbarColorGradient;
            }

            Logger.printDebug(() -> "Ignoring gradient colors: " + colorArrayToHex(colors)
                    + " positions: " + Arrays.toString(positions));
        }

        return colors;
    }

    /**
     * Injection point.
     *
     * Overrides color when video player seekbar is clicked.
     */
    public static int getVideoPlayerSeekbarClickedColor(int colorValue) {
        if (!SEEKBAR_CUSTOM_COLOR_ENABLED) {
            return colorValue;
        }

        return colorValue == ORIGINAL_SEEKBAR_COLOR
                ? seekbarColor
                : colorValue;
    }

    /**
     * Injection point.
     *
     * Overrides color used for the video player seekbar.
     */
    public static int getVideoPlayerSeekbarColor(int originalColor) {
        if (!SEEKBAR_CUSTOM_COLOR_ENABLED) {
            return originalColor;
        }

        return getSeekbarColorValue(originalColor);
    }

    /**
     * Color parameter is changed to the custom seekbar color, while retaining
     * the brightness and alpha changes of the parameter value compared to the original seekbar color.
     */
    private static int getSeekbarColorValue(int originalColor) {
        try {
            if (!SEEKBAR_CUSTOM_COLOR_ENABLED || originalColor == seekbarColor) {
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

    /** @noinspection SameParameterValue */
    private static int clamp(int value, int lower, int upper) {
        return Math.max(lower, Math.min(value, upper));
    }

    /** @noinspection SameParameterValue */
    private static float clamp(float value, float lower, float upper) {
        return Math.max(lower, Math.min(value, upper));
    }
}