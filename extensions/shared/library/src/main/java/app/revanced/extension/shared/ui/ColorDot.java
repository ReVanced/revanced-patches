package app.revanced.extension.shared.ui;

import static app.revanced.extension.shared.Utils.adjustColorBrightness;
import static app.revanced.extension.shared.Utils.getAppBackgroundColor;
import static app.revanced.extension.shared.Utils.isDarkModeEnabled;
import static app.revanced.extension.shared.settings.preference.ColorPickerPreference.DISABLED_ALPHA;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.ColorInt;

public class ColorDot {
    private static final int STROKE_WIDTH = Dim.dp(1.5f);

    /**
     * Creates a circular drawable with a main fill and a stroke.
     * Stroke adapts to dark/light theme and transparency, applied only when color is transparent or matches app background.
     */
    public static GradientDrawable createColorDotDrawable(@ColorInt int color) {
        final boolean isDarkTheme = isDarkModeEnabled();
        final boolean isTransparent = Color.alpha(color) == 0;
        final int opaqueColor = color | 0xFF000000;
        final int appBackground = getAppBackgroundColor();
        final int strokeColor;
        final int strokeWidth;

        // Determine stroke color.
        if (isTransparent || (opaqueColor == appBackground)) {
            final int baseColor = isTransparent ? appBackground : opaqueColor;
            strokeColor = adjustColorBrightness(baseColor, isDarkTheme ? 1.2f : 0.8f);
            strokeWidth = STROKE_WIDTH;
        } else {
            strokeColor = 0;
            strokeWidth = 0;
        }

        // Create circular drawable with conditional stroke.
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        circle.setStroke(strokeWidth, strokeColor);

        return circle;
    }

    /**
     * Applies the color dot drawable to the target view.
     */
    public static void applyColorDot(View targetView, @ColorInt int color, boolean enabled) {
        if (targetView == null) return;
        targetView.setBackground(createColorDotDrawable(color));
        targetView.setAlpha(enabled ? 1.0f : DISABLED_ALPHA);
        if (!isDarkModeEnabled()) {
            targetView.setClipToOutline(true);
            targetView.setElevation(Dim.dp2);
        }
    }
}
