package app.revanced.extension.shared.ui;

import static app.revanced.extension.shared.Utils.*;
import static app.revanced.extension.shared.settings.preference.ColorPickerPreference.DISABLED_ALPHA;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;

public class ColorDot {
    private static final int STROKE_WIDTH_DP = 1; // Stroke width in dp.

    /**
     * Creates a circular drawable with a stroke, background fill, and optional main fill.
     * In dark theme, stroke uses background color for transparent main fill, otherwise transparent.
     * In light theme, stroke uses adjusted color.
     */
    public static LayerDrawable createColorDotDrawable(int color) {
        int insetPx = dipToPixels(STROKE_WIDTH_DP); // Inset for inner circles.

        // Determine theme and stroke color.
        boolean isDarkTheme = isDarkModeEnabled();
        boolean isTransparent = (color >>> 24) == 0;
        int baseColor = isTransparent ? getAppBackgroundColor() : color;
        int strokeColor = isDarkTheme && isTransparent
                ? adjustColorBrightness(baseColor, 1.2f)
                : isDarkTheme ? Color.TRANSPARENT : adjustColorBrightness(baseColor | 0xFF000000, 0.8f);

        // Stroke circle.
        GradientDrawable stroke = new GradientDrawable();
        stroke.setShape(GradientDrawable.OVAL);
        stroke.setColor(strokeColor);

        // Background fill circle.
        GradientDrawable bgFill = new GradientDrawable();
        bgFill.setShape(GradientDrawable.OVAL);
        bgFill.setColor(getAppBackgroundColor());

        // Main fill circle.
        GradientDrawable mainFill = null;
        if (!isTransparent) {
            mainFill = new GradientDrawable();
            mainFill.setShape(GradientDrawable.OVAL);
            mainFill.setColor(color);
        }

        // Create layers: stroke, background fill, optional main fill.
        LayerDrawable layer = new LayerDrawable(mainFill != null
                ? new GradientDrawable[]{stroke, bgFill, mainFill}
                : new GradientDrawable[]{stroke, bgFill});

        // Apply insets to inner circles for effective size.
        layer.setLayerInset(1, insetPx, insetPx, insetPx, insetPx);
        if (mainFill != null) {
            layer.setLayerInset(2, insetPx, insetPx, insetPx, insetPx);
        }

        return layer;
    }

    /**
     * Applies the color dot drawable to the target view.
     */
    public static void applyColorDot(View targetView, int color, boolean enabled) {
        if (targetView == null) return;
        targetView.setBackground(createColorDotDrawable(color));
        targetView.setAlpha(enabled ? 1.0f : DISABLED_ALPHA);
        targetView.setClipToOutline(true);
        targetView.setElevation(dipToPixels(4));
    }
}
