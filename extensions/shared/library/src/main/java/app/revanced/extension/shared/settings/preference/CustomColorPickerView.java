package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.Utils.dipToPixels;

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

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

/**
 * A custom color picker view that allows the user to select a color using a hue slider and a saturation-value selector.
 * This implementation is density-independent and responsive across different screen sizes and DPIs.
 *
 * <p>
 * This view displays two main components for color selection:
 * <ul>
 *     <li><b>Hue Bar:</b> A vertical bar on the right that allows the user to select the hue component of the color.
 *     <li><b>Saturation-Value Selector:</b> A rectangular area that allows the user to select the saturation and value (brightness)
 *     components of the color based on the selected hue.
 * </ul>
 *
 * <p>
 * The view uses {@link LinearGradient} and {@link ComposeShader} to create the color gradients for the hue bar and the
 * saturation-value selector. It also uses {@link Paint} to draw the selectors (draggable handles).
 *
 * <p>
 * The selected color can be retrieved using {@link #getColor()} and can be set using {@link #setColor(int)}.
 * An {@link OnColorChangedListener} can be registered to receive notifications when the selected color changes.
 */
public class CustomColorPickerView extends View {

    /**
     * Interface definition for a callback to be invoked when the selected color changes.
     */
    public interface OnColorChangedListener {
        /**
         * Called when the selected color has changed.
         *
         * @param color The new selected color.
         */
        void onColorChanged(int color);
    }

    /** Pixel dimensions calculated from DP values */
    private static final float HUE_BAR_WIDTH = dipToPixels(12f);
    private static final float MARGIN_BETWEEN_AREAS = dipToPixels(24f);
    private static final float VIEW_PADDING = dipToPixels(16f);
    private static final float SELECTOR_RADIUS = dipToPixels(12f);
    private static final float HUE_CORNER_RADIUS = dipToPixels(6f);

    private static final int[] HUE_COLORS = new int[361];
    static {
        for (int i = 0; i <= 360; i++) {
            HUE_COLORS[i] = Color.HSVToColor(new float[]{i, 1, 1});
        }
    }

    /** Paint object used to draw the hue bar. */
    private final Paint huePaint;
    /** Paint object used to draw the saturation-value selector. */
    private final Paint saturationValuePaint;
    /** Paint object used to draw the draggable handles. */
    private final Paint selectorPaint;

    /** Rectangle representing the bounds of the hue bar. */
    private final RectF hueRect;
    /** Rectangle representing the bounds of the saturation-value selector. */
    private final RectF saturationValueRect;

    /** Reusable array for HSV color calculations to avoid allocations during drawing */
    private final float[] hsvArray = new float[3];

    /** Current hue value (0-360). */
    private float hue = 0f;
    /** Current saturation value (0-1). */
    private float saturation = 1f;
    /** Current value (brightness) value (0-1). */
    private float value = 1f;

    /** The currently selected color in ARGB format. */
    private int selectedColor = Color.HSVToColor(new float[]{hue, saturation, value});
    /** Listener to be notified when the selected color changes. */
    private OnColorChangedListener colorChangedListener;

    /** Track if we're currently dragging the hue or saturation handle */
    private boolean isDraggingHue = false;
    private boolean isDraggingSaturation = false;

    /**
     * Constructor for creating a CustomColorPickerView programmatically.
     *
     * @param context The Context the view is running in.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public CustomColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize the paint for the hue bar with antialiasing enabled.
        huePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Initialize the paint for the saturation-value selector with antialiasing enabled.
        saturationValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Initialize the paint for the draggable handles with antialiasing, fill-and-stroke style.
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        selectorPaint.setStrokeWidth(8f);
        // The stroke color (white border) will be applied in onDraw.

        // Initialize the rectangle objects for the different components.
        hueRect = new RectF();
        saturationValueRect = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final float DESIRED_ASPECT_RATIO = 0.8f; // height = width * 0.8

        final int minWidth = (int) Utils.dipToPixels(250);
        final int minHeight = (int) (minWidth * DESIRED_ASPECT_RATIO);

        int width = resolveSize(minWidth, widthMeasureSpec);
        int height = resolveSize(minHeight, heightMeasureSpec);

        // Ensure minimum dimensions for usability
        width = Math.max(width, minWidth);
        height = Math.max(height, minHeight);

        // Adjust height to maintain desired aspect ratio if possible
        final int desiredHeight = (int) (width * DESIRED_ASPECT_RATIO);
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    /**
     * Called when the size of the view changes.
     * This method calculates and sets the bounds of the hue bar and saturation-value selector.
     * It also creates the necessary shaders for the gradients.
     *
     * @param width    Current width of this view.
     * @param height    Current height of this view.
     * @param oldWidth Old width of this view.
     * @param oldHeight Old height of this view.
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        // Calculate bounds with hue bar on the right
        final float effectiveWidth = width - (2 * VIEW_PADDING);
        final float selectorWidth = effectiveWidth - HUE_BAR_WIDTH - MARGIN_BETWEEN_AREAS;

        // Adjust rectangles to account for padding and density-independent dimensions
        saturationValueRect.set(
                VIEW_PADDING,
                VIEW_PADDING,
                VIEW_PADDING + selectorWidth,
                height - VIEW_PADDING
        );

        hueRect.set(
                width - VIEW_PADDING - HUE_BAR_WIDTH,
                VIEW_PADDING,
                width - VIEW_PADDING,
                height - VIEW_PADDING
        );

        // Update the shaders.
        updateHueShader();
        updateSaturationValueShader();
    }

    /**
     * Generates an array of colors representing the full hue spectrum (0-360 degrees).
     */
    private void updateHueShader() {
        LinearGradient hueShader = new LinearGradient(
                hueRect.left, hueRect.top,
                hueRect.left, hueRect.bottom,
                HUE_COLORS,
                null,
                Shader.TileMode.CLAMP
        );

        huePaint.setShader(hueShader);
    }

    /**
     * Updates the shader for the saturation-value selector based on the currently selected hue.
     * This method creates a combined shader that blends a saturation gradient with a value gradient.
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
     * Draws the color picker view on the canvas.
     * This method draws the saturation-value selector, the hue bar with rounded corners,
     * and the draggable handles.
     *
     * @param canvas The canvas on which to draw.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the saturation-value selector rectangle.
        canvas.drawRect(saturationValueRect, saturationValuePaint);

        // Draw the hue bar.
        canvas.drawRoundRect(hueRect, HUE_CORNER_RADIUS, HUE_CORNER_RADIUS, huePaint);

        // Draw the hue selector handle.
        final float hueSelectorX = hueRect.centerX();
        final float hueSelectorY = hueRect.top + (hue / 360f) * hueRect.height();

        // Use the reusable array for HSV color calculation.
        hsvArray[0] = hue;
        hsvArray[1] = 1f;
        hsvArray[2] = 1f;
        final int hueHandleColor = Color.HSVToColor(hsvArray);

        selectorPaint.setColor(hueHandleColor);
        canvas.drawCircle(hueSelectorX, hueSelectorY, SELECTOR_RADIUS, selectorPaint);

        // Draw a white border for the hue handle.
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setColor(Color.WHITE);
        canvas.drawCircle(hueSelectorX, hueSelectorY, SELECTOR_RADIUS, selectorPaint);

        // Reset the paint style.
        selectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Draw the saturation-value selector handle.
        final float satSelectorX = saturationValueRect.left + saturation * saturationValueRect.width();
        final float valSelectorY = saturationValueRect.top + (1 - value) * saturationValueRect.height();

        selectorPaint.setColor(selectedColor);
        canvas.drawCircle(satSelectorX, valSelectorY, SELECTOR_RADIUS, selectorPaint);

        // Draw a white border for the saturation handle.
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setColor(Color.WHITE);
        canvas.drawCircle(satSelectorX, valSelectorY, SELECTOR_RADIUS, selectorPaint);
        selectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    /**
     * Handles touch events on the view.
     * This method determines whether the touch event occurred within the hue bar or the saturation-value selector,
     * updates the corresponding values (hue, saturation, value), and invalidates the view to trigger a redraw.
     * <p>
     * In addition to testing if the touch is within the strict rectangles, an expanded hit area (by selectorRadius)
     * is used so that the draggable handles remain active even when half of the handle is outside the drawn bounds.
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @SuppressLint("ClickableViewAccessibility") // performClick is not overridden, but not needed in this case.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();
        Logger.printDebug(() -> "onTouchEvent action: " + action + " x: " + x + " y: " + y);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Calculate current handle positions.
                final float hueSelectorX = hueRect.centerX();
                final float hueSelectorY = hueRect.top + (hue / 360f) * hueRect.height();

                final float satSelectorX = saturationValueRect.left + saturation * saturationValueRect.width();
                final float valSelectorY = saturationValueRect.top + (1 - value) * saturationValueRect.height();

                // Create hit areas for both handles.
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

                // Check if the touch started on either handle.
                if (hueHitRect.contains(x, y)) {
                    isDraggingHue = true;
                    updateHueFromTouch(y);
                } else if (satValHitRect.contains(x, y)) {
                    isDraggingSaturation = true;
                    updateSaturationValueFromTouch(x, y);
                } else if (hueRect.contains(x, y)) {
                    isDraggingHue = true;
                    updateHueFromTouch(y);
                } else if (saturationValueRect.contains(x, y)) {
                    isDraggingSaturation = true;
                    updateSaturationValueFromTouch(x, y);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // Continue updating values even if touch moves outside the view.
                if (isDraggingHue) {
                    updateHueFromTouch(y);
                } else if (isDraggingSaturation) {
                    updateSaturationValueFromTouch(x, y);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDraggingHue = false;
                isDraggingSaturation = false;
                break;
        }

        return true;
    }

    /**
     * Updates the hue value based on touch position, clamping to valid range.
     *
     * @param y The y-coordinate of the touch position.
     */
    private void updateHueFromTouch(float y) {
        // Clamp y to the hue rectangle bounds.
        final float clampedY = Utils.clamp(y, hueRect.top, hueRect.bottom);
        final float updatedHue = ((clampedY - hueRect.top) / hueRect.height()) * 360f;
        if (hue == updatedHue) {
            return;
        }

        hue = updatedHue;
        updateSaturationValueShader();
        updateSelectedColor();
    }

    /**
     * Updates saturation and value based on touch position, clamping to valid range.
     *
     * @param x The x-coordinate of the touch position.
     * @param y The y-coordinate of the touch position.
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
        updateSelectedColor();
    }

    /**
     * Updates the selected color and notifies listeners.
     */
    private void updateSelectedColor() {
        selectedColor = Color.HSVToColor(new float[]{hue, saturation, value});
        if (colorChangedListener != null) {
            colorChangedListener.onColorChanged(selectedColor);
        }

        invalidate();
    }

    /**
     * Sets the currently selected color.
     *
     * @param color The color to set in ARGB format.
     */
    public void setColor(int color) {
        if (selectedColor == color) {
            return;
        }

        // Convert the ARGB color to HSV values.
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // Update the hue, saturation, and value.
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];

        // Update the selected color.
        selectedColor = color;

        // Update the saturation-value shader based on the new hue.
        updateSaturationValueShader();

        // Notify the listener if it's set.
        if (colorChangedListener != null) {
            colorChangedListener.onColorChanged(selectedColor);
        }

        // Invalidate the view to trigger a redraw.
        invalidate();
    }

    /**
     * Gets the currently selected color.
     *
     * @return The selected color in ARGB format.
     */
    public int getColor() {
        return selectedColor;
    }

    /**
     * Sets the listener to be notified when the selected color changes.
     *
     * @param listener The listener to set.
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.colorChangedListener = listener;
    }
}
