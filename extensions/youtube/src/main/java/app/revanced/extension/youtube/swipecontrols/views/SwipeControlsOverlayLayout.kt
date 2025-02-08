package app.revanced.extension.youtube.swipecontrols.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.RelativeLayout
import app.revanced.extension.shared.StringRef.str
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.swipecontrols.SwipeControlsConfigurationProvider
import app.revanced.extension.youtube.swipecontrols.misc.SwipeControlsOverlay
import kotlin.math.min
import kotlin.math.round

/**
 * Main overlay layout for displaying volume and brightness level.
 */
class SwipeControlsOverlayLayout(
    context: Context,
    private val config: SwipeControlsConfigurationProvider,
) : RelativeLayout(context), SwipeControlsOverlay {

    constructor(context: Context) : this(context, SwipeControlsConfigurationProvider(context))

    // Circular progress view to display the current progress (volume or brightness).
    private val feedbackProgressView: CircularProgressView

    // Icons for brightness and volume.
    private val autoBrightnessIcon: Drawable
    private val manualBrightnessIcon: Drawable
    private val mutedVolumeIcon: Drawable
    private val normalVolumeIcon: Drawable

    // Function to retrieve drawable resources by name.
    private fun getDrawable(name: String): Drawable {
        return resources.getDrawable(
            Utils.getResourceIdentifier(context, name, "drawable"),
            context.theme,
        )
    }

    init {
        // Initialize circular progress view with specific configurations.
        feedbackProgressView = CircularProgressView(
            context,
            config.overlayBackgroundOpacity,  // Background opacity for the overlay.
            config.showOnlyIconInOverlay,     // If true, hides text, showing only the icon.
            config.overlayTextSize.toFloat(), // Text size for overlay elements, converted from SP to float.
            config.overlayTextColor           // Foreground color of the overlay text and icon.

        ).apply {
            layoutParams = LayoutParams(300, 300).apply {
                addRule(CENTER_IN_PARENT, TRUE) // Center the progress view.
            }
            visibility = GONE // Initially hidden.
        }
        addView(feedbackProgressView)

        // Load drawable icons for brightness and volume.
        autoBrightnessIcon = getDrawable("revanced_ic_sc_brightness_auto")
        manualBrightnessIcon = getDrawable("revanced_ic_sc_brightness_manual")
        mutedVolumeIcon = getDrawable("revanced_ic_sc_volume_mute")
        normalVolumeIcon = getDrawable("revanced_ic_sc_volume_normal")
    }

    // Handler and callback to hide the feedback progress view after a delay.
    private val feedbackHideHandler = Handler(Looper.getMainLooper())
    private val feedbackHideCallback = Runnable {
        feedbackProgressView.visibility = GONE
    }

    /**
     * Displays the feedback view with the given value, progress, and icon.
     * @param value Text to display (percentage, number, or "Auto").
     * @param progress Progress value for the circular progress bar.
     * @param max Maximum value for the progress scale.
     * @param icon Icon to display (brightness or volume).
     * @param isBrightness True if the feedback is related to brightness, otherwise false.
     */
    private fun showFeedbackView(value: String, progress: Int, max: Int, icon: Drawable, isBrightness: Boolean) {
        feedbackHideHandler.removeCallbacks(feedbackHideCallback)
        feedbackHideHandler.postDelayed(feedbackHideCallback, config.overlayShowTimeoutMillis)
        feedbackProgressView.apply {
            setProgress(progress, max, value, isBrightness) // Set the progress and value.
            setIcon(icon) // Set the appropriate icon.
            visibility = VISIBLE // Show the feedback view.
        }
    }

    // Called when volume changes.
    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        val icon = if (newVolume == 0) mutedVolumeIcon else normalVolumeIcon
        showFeedbackView("$newVolume", newVolume, maximumVolume, icon, isBrightness = false)
    }

    // Called when brightness changes.
    override fun onBrightnessChanged(brightness: Double) {
        if (config.shouldLowestValueEnableAutoBrightness && brightness <= 0) {
            // Show "Auto" message for brightness when it's the lowest value.
            showFeedbackView(str("revanced_swipe_lowest_value_enable_auto_brightness_overlay_text"),
                0, 100, autoBrightnessIcon, isBrightness = true)
        } else {
            val brightnessValue = round(brightness).toInt()
            showFeedbackView("$brightnessValue%", brightnessValue, 100, manualBrightnessIcon, isBrightness = true)
        }
    }

    // Called when the swipe session starts.
    override fun onEnterSwipeSession() {
        if (config.shouldEnableHapticFeedback) {
            @Suppress("DEPRECATION")
            performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING,
            )
        }
    }
}

/**
 * Custom view for rendering a circular progress indicator with text and icon.
 */
class CircularProgressView @JvmOverloads constructor(
    context: Context,
    private val overlayBackgroundOpacity: Int,  // Background opacity.
    private val showOnlyIconInOverlay: Boolean, // If true, only the icon is displayed.
    private val overlayTextSize: Float,         // Text size.
    private val overlayTextColor: Int,          // Color for text and icon.
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint objects for drawing various elements of the circular progress view.
    private fun createPaint(color: Int, strokeCap: Paint.Cap = Paint.Cap.BUTT) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        this.color = color
        this.strokeCap = strokeCap
    }

    private val backgroundPaint = createPaint(0x33000000) // Semi-transparent outer ring.
    private val brightnessPaint = createPaint(0xBFFFA500.toInt(), Paint.Cap.ROUND) // Orange for brightness, 75% transparency.
    private val volumePaint     = createPaint(0xBF2196F3.toInt(), Paint.Cap.ROUND) // Blue for volume, 75% transparency.

    private val innerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = overlayBackgroundOpacity // Opacity for the background.
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = overlayTextColor // Color for the text.
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, overlayTextSize, resources.displayMetrics
        ) // Convert the text size from SP to pixels based on the display metrics.
    }

    private var progress = 0
    private var maxProgress = 100
    private var displayText: String = "0"
    private var isBrightness = true
    private var icon: Drawable? = null
    private val rectF = RectF() // Rectangle to define the bounds for drawing.

    // Set the progress, max value, and the text to display, along with the mode (brightness or volume).
    fun setProgress(value: Int, max: Int, text: String, isBrightnessMode: Boolean) {
        progress = value
        maxProgress = max
        displayText = shortenTextIfNeeded(text)
        isBrightness = isBrightnessMode
        invalidate()
    }

    // Set the icon to display on the progress view.
    fun setIcon(drawable: Drawable) {
        icon = drawable
        icon?.setTint(overlayTextColor) // Apply the foreground color to the icon.
        invalidate()
    }

    /**
     * Shorten the text if it is too long to fit inside the ring.
     * @param text The original text to display.
     * @return The shortened text, with "..." appended if it was shortened.
     */
    private fun shortenTextIfNeeded(text: String): String {
        val maxWidth = width * 0.5f // Maximum allowed width for text.
        var textToDisplay = text

        // Reduce the text length if it exceeds the maximum width.
        while (textPaint.measureText(textToDisplay) > maxWidth && textToDisplay.length > 4) {
            textToDisplay = textToDisplay.dropLast(1) // Remove the last character.
        }

        // Add "..." if the text was shortened.
        return if (textToDisplay != text) "$textToDisplay..." else textToDisplay
    }

    // Override the onDraw method to draw the progress view and its components.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        rectF.set(20f, 20f, size - 20f, size - 20f)

        canvas.drawOval(rectF, backgroundPaint) // Draw the outer ring.
        canvas.drawCircle(width / 2f, height / 2f, size / 3, innerBackgroundPaint) // Draw the inner circle.

        // Select the paint for drawing based on whether it's brightness or volume.
        val paint = if (isBrightness) brightnessPaint else volumePaint
        val sweepAngle = (progress.toFloat() / maxProgress) * 360
        canvas.drawArc(rectF, -90f, sweepAngle, false, paint) // Draw the progress arc.

        // Draw the icon in the center.
        icon?.let {
            val iconSize = 80
            val iconX = (width - iconSize) / 2
            val iconY = (height / 2) - if (showOnlyIconInOverlay) 40 else 90
            it.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
            it.draw(canvas)
        }

        // If not in icon-only mode, draw the text inside the ring.
        if (!showOnlyIconInOverlay) {
            canvas.drawText(displayText, width / 2f, height / 2f + 55f, textPaint)
        }
    }
}
