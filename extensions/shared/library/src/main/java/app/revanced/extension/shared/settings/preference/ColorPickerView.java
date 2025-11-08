package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.settings.preference.ColorPickerPreference.getColorString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ui.Dim;

/**
 * A custom color picker view that allows the user to select a color using a hue slider, a saturation-value selector
 * and an optional opacity slider.
 * This implementation is density-independent and responsive across different screen sizes and DPIs.
 * <p>
 * This view displays three main components for color selection:
 * <ul>
 *     <li><b>Hue Bar:</b> A horizontal bar at the bottom that allows the user to select the hue component of the color.
 *     <li><b>Saturation-Value Selector:</b> A rectangular area above the hue bar that allows the user to select the
 *     saturation and value (brightness) components of the color based on the selected hue.
 *     <li><b>Opacity Slider:</b> An optional horizontal bar below the hue bar that allows the user to adjust
 *     the opacity (alpha channel) of the color.
 * </ul>
 * <p>
 * The view uses {@link LinearGradient} and {@link ComposeShader} to create the color gradients for the hue bar,
 * opacity slider, and the saturation-value selector. It also uses {@link Paint} to draw the selectors (draggable handles).
 * <p>
 * The selected color can be retrieved using {@link #getColor()} and can be set using {@link #setColor(int)}.
 * An {@link OnColorChangedListener} can be registered to receive notifications when the selected color changes.
 */
public class ColorPickerView extends View {
    /**
     * Interface definition for a callback to be invoked when the selected color changes.
     */
    public interface OnColorChangedListener {
        /**
         * Called when the selected color has changed.
         */
        void onColorChanged(@ColorInt int color);
    }

    /** Expanded touch area for the hue and opacity bars to increase the touch-sensitive area. */
    public static final float TOUCH_EXPANSION = Dim.dp20;

    /** Margin between different areas of the view (saturation-value selector, hue bar, and opacity slider). */
    private static final float MARGIN_BETWEEN_AREAS = Dim.dp24;

    /** Padding around the view. */
    private static final float VIEW_PADDING = Dim.dp16;

    /** Height of the hue bar. */
    private static final float HUE_BAR_HEIGHT = Dim.dp12;

    /** Height of the opacity slider. */
    private static final float OPACITY_BAR_HEIGHT = Dim.dp12;

    /** Corner radius for the hue bar. */
    private static final float HUE_CORNER_RADIUS = Dim.dp6;

    /** Corner radius for the opacity slider. */
    private static final float OPACITY_CORNER_RADIUS = Dim.dp6;

    /** Radius of the selector handles. */
    private static final float SELECTOR_RADIUS = Dim.dp12;

    /** Stroke width for the selector handle outlines. */
    private static final float SELECTOR_STROKE_WIDTH = 8;

    /**
     * Hue and opacity fill radius. Use slightly smaller radius for the selector handle fill,
     * otherwise the anti-aliasing causes the fill color to bleed past the selector outline.
     */
    private static final float SELECTOR_FILL_RADIUS = SELECTOR_RADIUS - SELECTOR_STROKE_WIDTH / 2;

    /** Thin dark outline stroke width for the selector rings. */
    private static final float SELECTOR_EDGE_STROKE_WIDTH = 1;

    /** Radius for the outer edge of the selector rings, including stroke width. */
    public static final float SELECTOR_EDGE_RADIUS =
            SELECTOR_RADIUS + SELECTOR_STROKE_WIDTH / 2 + SELECTOR_EDGE_STROKE_WIDTH / 2;

    /** Selector outline inner color. */
    @ColorInt
    private static final int SELECTOR_OUTLINE_COLOR = Color.WHITE;

    /** Dark edge color for the selector rings. */
    @ColorInt
    private static final int SELECTOR_EDGE_COLOR = Color.parseColor("#CFCFCF");

    /** Precomputed array of hue colors for the hue bar (0-360 degrees). */
    private static final int[] HUE_COLORS = new int[361];
    static {
        for (int i = 0; i < 361; i++) {
            HUE_COLORS[i] = Color.HSVToColor(new float[]{i, 1, 1});
        }
    }

    /** Paint for the hue bar. */
    private final Paint huePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Paint for the opacity slider. */
    private final Paint opacityPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Paint for the saturation-value selector. */
    private final Paint saturationValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Paint for the draggable selector handles. */
    private final Paint selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    {
        selectorPaint.setStrokeWidth(SELECTOR_STROKE_WIDTH);
    }

    /** Bounds of the hue bar. */
    private final RectF hueRect = new RectF();

    /** Bounds of the opacity slider. */
    private final RectF opacityRect = new RectF();

    /** Bounds of the saturation-value selector. */
    private final RectF saturationValueRect = new RectF();

    /** HSV color calculations to avoid allocations during drawing. */
    private final float[] hsvArray = {1, 1, 1};

    /** Current hue value (0-360). */
    private float hue = 0f;

    /** Current saturation value (0-1). */
    private float saturation = 1f;

    /** Current value (brightness) value (0-1). */
    private float value = 1f;

    /** Current opacity value (0-1). */
    private float opacity = 1f;

    /** The currently selected color, including alpha channel if opacity slider is enabled. */
    @ColorInt
    private int selectedColor;

    /** Listener for color change events. */
    private OnColorChangedListener colorChangedListener;

    /** Tracks if the hue selector is being dragged. */
    private boolean isDraggingHue;

    /** Tracks if the saturation-value selector is being dragged. */
    private boolean isDraggingSaturation;

    /** Tracks if the opacity selector is being dragged. */
    private boolean isDraggingOpacity;

    /** Flag to enable/disable the opacity slider. */
    private boolean opacitySliderEnabled = false;

    public ColorPickerView(Context context) {
        super(context);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Enables or disables the opacity slider.
     */
    public void setOpacitySliderEnabled(boolean enabled) {
        if (opacitySliderEnabled != enabled) {
            opacitySliderEnabled = enabled;
            if (!enabled) {
                opacity = 1f; // Reset to fully opaque when disabled.
                updateSelectedColor();
            }
            updateOpacityShader();
            requestLayout(); // Trigger re-measure to account for opacity slider.
            invalidate();
        }
    }

    /**
     * Measures the view, ensuring a consistent aspect ratio and minimum dimensions.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final float DESIRED_ASPECT_RATIO = 0.8f; // height = width * 0.8

        final int minWidth = Dim.dp(250);
        final int minHeight = (int) (minWidth * DESIRED_ASPECT_RATIO) + (int) (HUE_BAR_HEIGHT + MARGIN_BETWEEN_AREAS)
                + (opacitySliderEnabled ? (int) (OPACITY_BAR_HEIGHT + MARGIN_BETWEEN_AREAS) : 0);

        int width = resolveSize(minWidth, widthMeasureSpec);
        int height = resolveSize(minHeight, heightMeasureSpec);

        // Ensure minimum dimensions for usability.
        width = Math.max(width, minWidth);
        height = Math.max(height, minHeight);

        // Adjust height to maintain desired aspect ratio if possible.
        final int desiredHeight = (int) (width * DESIRED_ASPECT_RATIO) + (int) (HUE_BAR_HEIGHT + MARGIN_BETWEEN_AREAS)
                + (opacitySliderEnabled ? (int) (OPACITY_BAR_HEIGHT + MARGIN_BETWEEN_AREAS) : 0);
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    /**
     * Updates the view's layout when its size changes, recalculating bounds and shaders.
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        // Calculate bounds with hue bar and optional opacity bar at the bottom.
        final float effectiveWidth = width - (2 * VIEW_PADDING);
        final float effectiveHeight = height - (2 * VIEW_PADDING) - HUE_BAR_HEIGHT - MARGIN_BETWEEN_AREAS
                - (opacitySliderEnabled ? OPACITY_BAR_HEIGHT + MARGIN_BETWEEN_AREAS : 0);

        // Adjust rectangles to account for padding and density-independent dimensions.
        saturationValueRect.set(
                VIEW_PADDING,
                VIEW_PADDING,
                VIEW_PADDING + effectiveWidth,
                VIEW_PADDING + effectiveHeight
        );

        hueRect.set(
                VIEW_PADDING,
                height - VIEW_PADDING - HUE_BAR_HEIGHT - (opacitySliderEnabled ? OPACITY_BAR_HEIGHT + MARGIN_BETWEEN_AREAS : 0),
                VIEW_PADDING + effectiveWidth,
                height - VIEW_PADDING - (opacitySliderEnabled ? OPACITY_BAR_HEIGHT + MARGIN_BETWEEN_AREAS : 0)
        );

        if (opacitySliderEnabled) {
            opacityRect.set(
                    VIEW_PADDING,
                    height - VIEW_PADDING - OPACITY_BAR_HEIGHT,
                    VIEW_PADDING + effectiveWidth,
                    height - VIEW_PADDING
            );
        }

        // Update the shaders.
        updateHueShader();
        updateSaturationValueShader();
        updateOpacityShader();
    }

    /**
     * Updates the shader for the hue bar to reflect the color gradient.
     */
    private void updateHueShader() {
        LinearGradient hueShader = new LinearGradient(
                hueRect.left, hueRect.top,
                hueRect.right, hueRect.top,
                HUE_COLORS,
                null,
                Shader.TileMode.CLAMP
        );

        huePaint.setShader(hueShader);
    }

    /**
     * Updates the shader for the opacity slider to reflect the current RGB color with varying opacity.
     */
    private void updateOpacityShader() {
        if (!opacitySliderEnabled) {
            opacityPaint.setShader(null);
            return;
        }

        // Create a linear gradient for opacity from transparent to opaque, using the current RGB color.
        int rgbColor = Color.HSVToColor(0, new float[]{hue, saturation, value});
        LinearGradient opacityShader = new LinearGradient(
                opacityRect.left, opacityRect.top,
                opacityRect.right, opacityRect.top,
                rgbColor & 0x00FFFFFF, // Fully transparent
                rgbColor | 0xFF000000, // Fully opaque
                Shader.TileMode.CLAMP
        );

        opacityPaint.setShader(opacityShader);
    }

    /**
     * Updates the shader for the saturation-value selector to reflect the current hue.
     */
    private void updateSaturationValueShader() {
        // Create a saturation-value gradient based on the current hue.
        // Calculate the start color (white with the selected hue) for the saturation gradient.
        final int startColor = Color.HSVToColor(new float[]{hue, 0f, 1f});

        // Calculate the middle color (fully saturated color with the selected hue) for the saturation gradient.
        final int midColor = Color.HSVToColor(new float[]{hue, 1f, 1f});

        // Create a linear gradient for the saturation from startColor to midColor (horizontal).
        LinearGradient satShader = new LinearGradient(
                saturationValueRect.left, saturationValueRect.top,
                saturationValueRect.right, saturationValueRect.top,
                startColor,
                midColor,
                Shader.TileMode.CLAMP
        );

        // Create a linear gradient for the value (brightness) from white to black (vertical).
        LinearGradient valShader = new LinearGradient(
                saturationValueRect.left, saturationValueRect.top,
                saturationValueRect.left, saturationValueRect.bottom,
                Color.WHITE,
                Color.BLACK,
                Shader.TileMode.CLAMP
        );

        // Combine the saturation and value shaders using PorterDuff.Mode.MULTIPLY to create the final color.
        ComposeShader combinedShader = new ComposeShader(satShader, valShader, PorterDuff.Mode.MULTIPLY);

        // Set the combined shader for the saturation-value paint.
        saturationValuePaint.setShader(combinedShader);
    }

    /**
     * Draws the color picker components, including the saturation-value selector, hue bar, opacity slider, and their respective handles.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the saturation-value selector rectangle.
        canvas.drawRect(saturationValueRect, saturationValuePaint);

        // Draw the hue bar.
        canvas.drawRoundRect(hueRect, HUE_CORNER_RADIUS, HUE_CORNER_RADIUS, huePaint);

        // Draw the opacity bar if enabled.
        if (opacitySliderEnabled) {
            canvas.drawRoundRect(opacityRect, OPACITY_CORNER_RADIUS, OPACITY_CORNER_RADIUS, opacityPaint);
        }

        final float hueSelectorX = hueRect.left + (hue / 360f) * hueRect.width();
        final float hueSelectorY = hueRect.centerY();

        final float satSelectorX = saturationValueRect.left + saturation * saturationValueRect.width();
        final float satSelectorY = saturationValueRect.top + (1 - value) * saturationValueRect.height();

        // Draw the saturation and hue selector handles filled with their respective colors (fully opaque).
        hsvArray[0] = hue;
        final int hueHandleColor = Color.HSVToColor(0xFF, hsvArray); // Force opaque for hue handle.
        final int satHandleColor = Color.HSVToColor(0xFF, new float[]{hue, saturation, value}); // Force opaque for sat-val handle.
        selectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        selectorPaint.setColor(hueHandleColor);
        canvas.drawCircle(hueSelectorX, hueSelectorY, SELECTOR_FILL_RADIUS, selectorPaint);

        selectorPaint.setColor(satHandleColor);
        canvas.drawCircle(satSelectorX, satSelectorY, SELECTOR_FILL_RADIUS, selectorPaint);

        if (opacitySliderEnabled) {
            final float opacitySelectorX = opacityRect.left + opacity * opacityRect.width();
            final float opacitySelectorY = opacityRect.centerY();
            selectorPaint.setColor(selectedColor); // Use full ARGB color to show opacity.
            canvas.drawCircle(opacitySelectorX, opacitySelectorY, SELECTOR_FILL_RADIUS, selectorPaint);
        }

        // Draw white outlines for the handles.
        selectorPaint.setColor(SELECTOR_OUTLINE_COLOR);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(SELECTOR_STROKE_WIDTH);
        canvas.drawCircle(hueSelectorX, hueSelectorY, SELECTOR_RADIUS, selectorPaint);
        canvas.drawCircle(satSelectorX, satSelectorY, SELECTOR_RADIUS, selectorPaint);
        if (opacitySliderEnabled) {
            final float opacitySelectorX = opacityRect.left + opacity * opacityRect.width();
            final float opacitySelectorY = opacityRect.centerY();
            canvas.drawCircle(opacitySelectorX, opacitySelectorY, SELECTOR_RADIUS, selectorPaint);
        }

        // Draw thin dark outlines for the handles at the outer edge of the white outline.
        selectorPaint.setColor(SELECTOR_EDGE_COLOR);
        selectorPaint.setStrokeWidth(SELECTOR_EDGE_STROKE_WIDTH);
        canvas.drawCircle(hueSelectorX, hueSelectorY, SELECTOR_EDGE_RADIUS, selectorPaint);
        canvas.drawCircle(satSelectorX, satSelectorY, SELECTOR_EDGE_RADIUS, selectorPaint);
        if (opacitySliderEnabled) {
            final float opacitySelectorX = opacityRect.left + opacity * opacityRect.width();
            final float opacitySelectorY = opacityRect.centerY();
            canvas.drawCircle(opacitySelectorX, opacitySelectorY, SELECTOR_EDGE_RADIUS, selectorPaint);
        }
    }

    /**
     * Handles touch events to allow dragging of the hue, saturation-value, and opacity selectors.
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            final float x = event.getX();
            final float y = event.getY();
            final int action = event.getAction();
            Logger.printDebug(() -> "onTouchEvent action: " + action + " x: " + x + " y: " + y);

            // Define touch expansion for the hue and opacity bars.
            RectF expandedHueRect = new RectF(
                    hueRect.left,
                    hueRect.top - TOUCH_EXPANSION,
                    hueRect.right,
                    hueRect.bottom + TOUCH_EXPANSION
            );
            RectF expandedOpacityRect = opacitySliderEnabled ? new RectF(
                    opacityRect.left,
                    opacityRect.top - TOUCH_EXPANSION,
                    opacityRect.right,
                    opacityRect.bottom + TOUCH_EXPANSION
            ) : new RectF();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Calculate current handle positions.
                    final float hueSelectorX = hueRect.left + (hue / 360f) * hueRect.width();
                    final float hueSelectorY = hueRect.centerY();

                    final float satSelectorX = saturationValueRect.left + saturation * saturationValueRect.width();
                    final float valSelectorY = saturationValueRect.top + (1 - value) * saturationValueRect.height();

                    final float opacitySelectorX = opacitySliderEnabled ? opacityRect.left + opacity * opacityRect.width() : 0;
                    final float opacitySelectorY = opacitySliderEnabled ? opacityRect.centerY() : 0;

                    // Create hit areas for all handles.
                    RectF hueHitRect = new RectF(
                            hueSelectorX - SELECTOR_RADIUS,
                            hueSelectorY - SELECTOR_RADIUS,
                            hueSelectorX + SELECTOR_RADIUS,
                            hueSelectorY + SELECTOR_RADIUS
                    );
                    RectF satValHitRect = new RectF(
                            satSelectorX - SELECTOR_RADIUS,
                            valSelectorY - SELECTOR_RADIUS,
                            satSelectorX + SELECTOR_RADIUS,
                            valSelectorY + SELECTOR_RADIUS
                    );
                    RectF opacityHitRect = opacitySliderEnabled ? new RectF(
                            opacitySelectorX - SELECTOR_RADIUS,
                            opacitySelectorY - SELECTOR_RADIUS,
                            opacitySelectorX + SELECTOR_RADIUS,
                            opacitySelectorY + SELECTOR_RADIUS
                    ) : new RectF();

                    // Check if the touch started on a handle or within the expanded bar areas.
                    if (hueHitRect.contains(x, y)) {
                        isDraggingHue = true;
                        updateHueFromTouch(x);
                    } else if (satValHitRect.contains(x, y)) {
                        isDraggingSaturation = true;
                        updateSaturationValueFromTouch(x, y);
                    } else if (opacitySliderEnabled && opacityHitRect.contains(x, y)) {
                        isDraggingOpacity = true;
                        updateOpacityFromTouch(x);
                    } else if (expandedHueRect.contains(x, y)) {
                        // Handle touch within the expanded hue bar area.
                        isDraggingHue = true;
                        updateHueFromTouch(x);
                    } else if (saturationValueRect.contains(x, y)) {
                        isDraggingSaturation = true;
                        updateSaturationValueFromTouch(x, y);
                    } else if (opacitySliderEnabled && expandedOpacityRect.contains(x, y)) {
                        isDraggingOpacity = true;
                        updateOpacityFromTouch(x);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Continue updating values even if touch moves outside the view.
                    if (isDraggingHue) {
                        updateHueFromTouch(x);
                    } else if (isDraggingSaturation) {
                        updateSaturationValueFromTouch(x, y);
                    } else if (isDraggingOpacity) {
                        updateOpacityFromTouch(x);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isDraggingHue = false;
                    isDraggingSaturation = false;
                    isDraggingOpacity = false;
                    break;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onTouchEvent failure", ex);
        }

        return true;
    }

    /**
     * Updates the hue value based on a touch event.
     */
    private void updateHueFromTouch(float x) {
        // Clamp x to the hue rectangle bounds.
        final float clampedX = Utils.clamp(x, hueRect.left, hueRect.right);
        final float updatedHue = ((clampedX - hueRect.left) / hueRect.width()) * 360f;
        if (hue == updatedHue) {
            return;
        }

        hue = updatedHue;
        updateSaturationValueShader();
        updateOpacityShader();
        updateSelectedColor();
    }

    /**
     * Updates the saturation and value based on a touch event.
     */
    private void updateSaturationValueFromTouch(float x, float y) {
        // Clamp x and y to the saturation-value rectangle bounds.
        final float clampedX = Utils.clamp(x, saturationValueRect.left, saturationValueRect.right);
        final float clampedY = Utils.clamp(y, saturationValueRect.top, saturationValueRect.bottom);

        final float updatedSaturation = (clampedX - saturationValueRect.left) / saturationValueRect.width();
        final float updatedValue = 1 - ((clampedY - saturationValueRect.top) / saturationValueRect.height());

        if (saturation == updatedSaturation && value == updatedValue) {
            return;
        }
        saturation = updatedSaturation;
        value = updatedValue;
        updateOpacityShader();
        updateSelectedColor();
    }

    /**
     * Updates the opacity value based on a touch event.
     */
    private void updateOpacityFromTouch(float x) {
        if (!opacitySliderEnabled) {
            return;
        }
        final float clampedX = Utils.clamp(x, opacityRect.left, opacityRect.right);
        final float updatedOpacity = (clampedX - opacityRect.left) / opacityRect.width();
        if (opacity == updatedOpacity) {
            return;
        }
        opacity = updatedOpacity;
        updateSelectedColor();
    }

    /**
     * Updates the selected color based on the current hue, saturation, value, and opacity.
     */
    private void updateSelectedColor() {
        final int rgbColor = Color.HSVToColor(0, new float[]{hue, saturation, value});
        final int updatedColor = opacitySliderEnabled
                ? (rgbColor & 0x00FFFFFF) | (((int) (opacity * 255)) << 24)
                : (rgbColor & 0x00FFFFFF) | 0xFF000000;

        if (selectedColor != updatedColor) {
            selectedColor = updatedColor;

            if (colorChangedListener != null) {
                colorChangedListener.onColorChanged(updatedColor);
            }
        }

        // Must always redraw, otherwise if saturation is pure grey or black
        // then the hue slider cannot be changed.
        invalidate();
    }

    /**
     * Sets the selected color, updating the hue, saturation, value and opacity sliders accordingly.
     */
    public void setColor(@ColorInt int color) {
        if (selectedColor == color) {
            return;
        }

        // Update the selected color.
        selectedColor = color;
        Logger.printDebug(() -> "setColor: " + getColorString(selectedColor, opacitySliderEnabled));

        // Convert the ARGB color to HSV values.
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // Update the hue, saturation, and value.
        hue = hsv[0];
        saturation = hsv[1];
        value = hsv[2];
        opacity = opacitySliderEnabled ? ((color >> 24) & 0xFF) / 255f : 1f;

        // Update the saturation-value shader based on the new hue.
        updateSaturationValueShader();
        updateOpacityShader();

        // Notify the listener if it's set.
        if (colorChangedListener != null) {
            colorChangedListener.onColorChanged(selectedColor);
        }

        // Invalidate the view to trigger a redraw.
        invalidate();
    }

    /**
     * Gets the currently selected color.
     */
    @ColorInt
    public int getColor() {
        return selectedColor;
    }

    /**
     * Sets a listener to be notified when the selected color changes.
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        colorChangedListener = listener;
    }
}
