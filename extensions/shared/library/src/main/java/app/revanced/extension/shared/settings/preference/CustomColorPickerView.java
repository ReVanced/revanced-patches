package app.revanced.extension.shared.settings.preference;

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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import app.revanced.extension.shared.Utils;

/**
 * A custom color picker view that allows the user to select a color using a hue slider and a saturation-value selector.
 * This implementation is density-independent and responsive across different screen sizes and DPIs.
 *
 * <p>
 * This view displays three main components for color selection:
 * <ul>
 *     <li><b>Hue Bar:</b> A horizontal bar at the bottom that allows the user to select the hue component of the color.</li>
 *     <li><b>Saturation-Value Selector:</b> A rectangular area (on the right) that allows the user to select the saturation and value (brightness)
 *     components of the color based on the selected hue.</li>
 *     <li><b>Color Previews:</b> Two vertical rectangles on the left. The top shows the original/current color and the bottom shows the new color selected.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The view uses {@link LinearGradient} and {@link ComposeShader} to create the color gradients for the hue bar and the
 * saturation-value selector. It also uses {@link Paint} to draw the selectors (draggable handles) and preview rectangles.
 * </p>
 *
 * <p>
 * The selected color can be retrieved using {@link #getColor()} and can be set using {@link #setColor(int)}.
 * An {@link OnColorChangedListener} can be registered to receive notifications when the selected color changes.
 * </p>
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

    /** Paint object used to draw the hue bar. */
    private Paint huePaint;
    /** Paint object used to draw the saturation-value selector. */
    private Paint saturationValuePaint;
    /** Paint object used to draw the draggable handles. */
    private Paint selectorPaint;
    /** Paint object used to fill the preview rectangles. */
    private Paint previewPaint;

    /** Rectangle representing the bounds of the hue bar. */
    private RectF hueRect;
    /** Rectangle representing the bounds of the saturation-value selector. */
    private RectF saturationValueRect;
    /** Rectangle representing the preview area for original color (top left). */
    private RectF previewOriginalRect;
    /** Rectangle representing the preview area for new color (bottom left). */
    private RectF previewNewRect;

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
    /** The original color (before the user starts modifying). */
    private int originalColor = selectedColor;
    /** Listener to be notified when the selected color changes. */
    private OnColorChangedListener colorChangedListener;

    /** Track if we're currently dragging the hue or saturation handle */
    private boolean isDraggingHue = false;
    private boolean isDraggingSaturation = false;

    /** Pixel dimensions calculated from DP values */
    private float hueBarHeight;
    private float marginBetweenAreas;
    private float previewWidth;
    private float viewPadding;
    private float selectorRadius;
    private float hueCornerRadius;

    /**
     * Constructor for creating a CustomColorPickerView programmatically.
     *
     * @param context The Context the view is running in.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public CustomColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDimensions();
        init();
    }

    /**
     * Initializes density-independent dimensions by converting them to pixels.
     */
    private void initDimensions() {
        // Density-independent dimensions converted to pixels
        float HUE_BAR_HEIGHT_DP = 12f;
        float MARGIN_BETWEEN_AREAS_DP = 24f;
        float PREVIEW_WIDTH_DP = 24f;
        float VIEW_PADDING_DP = 16f;
        float SELECTOR_RADIUS_DP = 12f;
        float HUE_CORNER_RADIUS_DP = 6f;

        hueBarHeight = dpToPx(HUE_BAR_HEIGHT_DP);
        marginBetweenAreas = dpToPx(MARGIN_BETWEEN_AREAS_DP);
        previewWidth = dpToPx(PREVIEW_WIDTH_DP);
        viewPadding = dpToPx(VIEW_PADDING_DP);
        selectorRadius = dpToPx(SELECTOR_RADIUS_DP);
        hueCornerRadius = dpToPx(HUE_CORNER_RADIUS_DP);
    }

    /**
     * Converts dp value to pixels.
     *
     * @param dp The density-independent pixels value
     * @return The pixel value
     */
    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float DESIRED_ASPECT_RATIO = 1.2f; // height = width * 1.2

        int minWidth = (int) dpToPx(250);
        int minHeight = (int) (minWidth * DESIRED_ASPECT_RATIO);

        int width = resolveSize(minWidth, widthMeasureSpec);
        int height = resolveSize(minHeight, heightMeasureSpec);

        // Ensure minimum dimensions for usability
        width = Math.max(width, minWidth);
        height = Math.max(height, minHeight);

        // Adjust height to maintain desired aspect ratio if possible
        int desiredHeight = (int) (width * DESIRED_ASPECT_RATIO);
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    /**
     * Initializes the paint objects and the rectangle bounds.
     */
    private void init() {
        // Initialize the paint for the hue bar with antialiasing enabled.
        huePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Initialize the paint for the saturation-value selector with antialiasing enabled.
        saturationValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Initialize the paint for the draggable handles with antialiasing, fill-and-stroke style.
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        selectorPaint.setStrokeWidth(8f);
        // The stroke color (white border) will be applied in onDraw.

        // Initialize the paint for filling the preview rectangles.
        previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStyle(Paint.Style.FILL);

        // Initialize the rectangle objects for the different components.
        hueRect = new RectF();
        saturationValueRect = new RectF();
        previewOriginalRect = new RectF();
        previewNewRect = new RectF();
    }

    /**
     * Called when the size of the view changes.
     * This method calculates and sets the bounds of the hue bar, saturation-value selector, and the preview rectangles.
     * It also creates the necessary shaders for the gradients.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Reduce the space taken by hue bar and margins to give more room for the color area
        float effectiveHeight = h - (2 * viewPadding);

        // Calculate optimal preview rectangle height
        float previewHeight = effectiveHeight - hueBarHeight - marginBetweenAreas;

        // Add a small gap between preview and saturation-value selector
        float gapBetweenPreviewAndSelector = viewPadding / 2;

        // Adjust all rectangles to account for padding and density-independent dimensions
        previewOriginalRect.set(
                viewPadding,
                viewPadding,
                viewPadding + previewWidth,
                viewPadding + previewHeight / 2
        );

        previewNewRect.set(
                viewPadding,
                viewPadding + previewHeight / 2,
                viewPadding + previewWidth,
                viewPadding + previewHeight
        );

        saturationValueRect.set(
                viewPadding + previewWidth + gapBetweenPreviewAndSelector,
                viewPadding,
                w - viewPadding,
                viewPadding + previewHeight
        );

        hueRect.set(
                viewPadding,
                h - viewPadding - hueBarHeight,
                w - viewPadding,
                h - viewPadding
        );

        // Update the shaders
        updateHueShader();
        updateSaturationValueShader();
    }

    /**
     * Generates an array of colors representing the full hue spectrum (0-360 degrees).
     */
    private void updateHueShader() {
        int[] hueColors = new int[361];
        for (int i = 0; i <= 360; i++) {
            hueColors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }

        LinearGradient hueShader = new LinearGradient(
                hueRect.left, hueRect.top,
                hueRect.right, hueRect.top,
                hueColors,
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
        int startColor = Color.HSVToColor(new float[]{hue, 0f, 1f});

        // Calculate the middle color (fully saturated color with the selected hue) for the saturation gradient.
        int midColor = Color.HSVToColor(new float[]{hue, 1f, 1f});

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
     * This method draws the preview rectangles, the saturation-value selector, the hue bar with rounded corners,
     * and the draggable handles.
     *
     * @param canvas The canvas on which to draw.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the original color preview rectangle (left top)
        previewPaint.setColor(originalColor);
        canvas.drawRect(previewOriginalRect, previewPaint);

        // Draw the new color preview rectangle (left bottom)
        previewPaint.setColor(selectedColor);
        canvas.drawRect(previewNewRect, previewPaint);

        // Draw the saturation-value selector rectangle
        canvas.drawRect(saturationValueRect, saturationValuePaint);

        // Draw the hue bar
        canvas.drawRoundRect(hueRect, hueCornerRadius, hueCornerRadius, huePaint);

        // Draw the hue selector handle
        float hueSelectorX = hueRect.left + (hue / 360f) * hueRect.width();
        float hueSelectorY = hueRect.centerY();

        // Use the reusable array for HSV color calculation
        hsvArray[0] = hue;
        hsvArray[1] = 1f;
        hsvArray[2] = 1f;
        int hueHandleColor = Color.HSVToColor(hsvArray);

        selectorPaint.setColor(hueHandleColor);
        canvas.drawCircle(hueSelectorX, hueSelectorY, selectorRadius, selectorPaint);

        // Draw a white border for the hue handle
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setColor(Color.WHITE);
        canvas.drawCircle(hueSelectorX, hueSelectorY, selectorRadius, selectorPaint);

        // Reset the paint style
        selectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Draw the saturation-value selector handle
        float satSelectorX = saturationValueRect.left + saturation * saturationValueRect.width();
        float valSelectorY = saturationValueRect.top + (1 - value) * saturationValueRect.height();

        selectorPaint.setColor(selectedColor);
        canvas.drawCircle(satSelectorX, valSelectorY, selectorRadius, selectorPaint);

        // Draw a white border for the saturation handle
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setColor(Color.WHITE);
        canvas.drawCircle(satSelectorX, valSelectorY, selectorRadius, selectorPaint);
        selectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    /**
     * Handles touch events on the view.
     * This method determines whether the touch event occurred within the hue bar or the saturation-value selector,
     * updates the corresponding values (hue, saturation, value), and invalidates the view to trigger a redraw.
     * <p>
     * In addition to testing if the touch is within the strict rectangles, an expanded hit area (by selectorRadius)
     * is used so that the draggable handles remain active even when half of the handle is outside the drawn bounds.
     * </p>
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @SuppressLint("ClickableViewAccessibility") // performClick is not overridden, but not needed in this case.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // Calculate current handle positions
        float hueSelectorX = hueRect.left + (hue / 360f) * hueRect.width();
        float hueSelectorY = hueRect.centerY();

        float satSelectorX = saturationValueRect.left + saturation * saturationValueRect.width();
        float valSelectorY = saturationValueRect.top + (1 - value) * saturationValueRect.height();

        // Create hit areas for both handles
        RectF hueHitRect = new RectF(
                hueSelectorX - selectorRadius,
                hueSelectorY - selectorRadius,
                hueSelectorX + selectorRadius,
                hueSelectorY + selectorRadius
        );

        RectF satValHitRect = new RectF(
                satSelectorX - selectorRadius,
                valSelectorY - selectorRadius,
                satSelectorX + selectorRadius,
                valSelectorY + selectorRadius
        );

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if the touch started on either handle
                if (hueHitRect.contains(x, y)) {
                    isDraggingHue = true;
                    updateHueFromTouch(x);
                } else if (satValHitRect.contains(x, y)) {
                    isDraggingSaturation = true;
                    updateSaturationValueFromTouch(x, y);
                } else if (hueRect.contains(x, y)) {
                    isDraggingHue = true;
                    updateHueFromTouch(x);
                } else if (saturationValueRect.contains(x, y)) {
                    isDraggingSaturation = true;
                    updateSaturationValueFromTouch(x, y);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // Continue updating values even if touch moves outside the view
                if (isDraggingHue) {
                    updateHueFromTouch(x);
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
     * Updates the hue value based on touch position, clamping to valid range
     * @param x The x-coordinate of the touch position
     */
    private void updateHueFromTouch(float x) {
        // Clamp x to the hue rectangle bounds
        float clampedX = Math.max(hueRect.left, Math.min(hueRect.right, x));
        hue = ((clampedX - hueRect.left) / hueRect.width()) * 360f;
        updateSaturationValueShader();
        updateSelectedColor();
    }

    /**
     * Updates saturation and value based on touch position, clamping to valid range
     * @param x The x-coordinate of the touch position
     * @param y The y-coordinate of the touch position
     */
    private void updateSaturationValueFromTouch(float x, float y) {
        // Clamp x and y to the saturation-value rectangle bounds
        final float clampedX = Utils.clamp(x, saturationValueRect.left, saturationValueRect.right);
        final float clampedY = Utils.clamp(x, saturationValueRect.top, saturationValueRect.bottom);

        saturation = (clampedX - saturationValueRect.left) / saturationValueRect.width();
        value = 1 - ((clampedY - saturationValueRect.top) / saturationValueRect.height());
        updateSelectedColor();
    }

    /**
     * Updates the selected color and notifies listeners
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
        // Convert the ARGB color to HSV values.
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // Update the hue, saturation, and value.
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];

        // Update the selected color.
        selectedColor = color;
        // Also update the original color if this is the initial load.
        originalColor = color;

        // Update the saturation-value shader based on the new hue.
        updateSaturationValueShader();

        // Invalidate the view to trigger a redraw.
        invalidate();

        // Notify the listener if it's set.
        if (colorChangedListener != null) {
            colorChangedListener.onColorChanged(selectedColor);
        }
    }

    /**
     * Sets the initial color without updating the selection.
     * This is used to show the original/current color in the preview.
     *
     * @param color The initial color in ARGB format.
     */
    public void setInitialColor(int color) {
        originalColor = color;
        // Also update the current selection.
        setColor(color);
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
