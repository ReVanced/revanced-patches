package app.revanced.extension.shared.ui;

import static app.revanced.extension.shared.Utils.*;
import static app.revanced.extension.shared.settings.preference.ColorPickerPreference.DISABLED_ALPHA;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class ColorDot {
    private static final int STROKE_WIDTH = dipToPixels(1); // Stroke width in dp.

    /**
     * Creates a circular drawable with a main fill and a stroke.
     * Stroke adapts to dark/light theme and transparency.
     */
    public static GradientDrawable createColorDotDrawable(int color) {
        boolean isDarkTheme = isDarkModeEnabled();
        boolean isTransparent = (color >>> 24) == 0;
        int appBackground = getAppBackgroundColor();
        int baseColor = isTransparent ? appBackground : (color | 0xFF000000);

        // Determine stroke color.
        int strokeColor = isTransparent || ((color | 0xFF000000) == appBackground)
                ? adjustColorBrightness(baseColor, isDarkTheme ? 1.2f : 0.8f)
                : baseColor;

        // Create circular drawable with stroke.
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        circle.setStroke(STROKE_WIDTH, strokeColor);

        return circle;
    }

    /**
     * Applies the color dot drawable to the target view.
     */
    public static void applyColorDot(View targetView, int color, boolean enabled) {
        if (targetView == null) return;
        targetView.setBackground(createColorDotDrawable(color));
        targetView.setAlpha(enabled ? 1.0f : DISABLED_ALPHA);
        targetView.setClipToOutline(true);
        targetView.setElevation(dipToPixels(2));
    }
}
