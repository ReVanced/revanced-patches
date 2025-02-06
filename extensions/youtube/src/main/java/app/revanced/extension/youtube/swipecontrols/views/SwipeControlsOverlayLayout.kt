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

class SwipeControlsOverlayLayout(
    context: Context,
    private val config: SwipeControlsConfigurationProvider,
) : RelativeLayout(context), SwipeControlsOverlay {

    constructor(context: Context) : this(context, SwipeControlsConfigurationProvider(context))

    private val feedbackProgressView: CircularProgressView

    // Icons for brightness and volume
    private val autoBrightnessIcon: Drawable
    private val manualBrightnessIcon: Drawable
    private val mutedVolumeIcon: Drawable
    private val normalVolumeIcon: Drawable

    private fun getDrawable(name: String): Drawable {
        return resources.getDrawable(
            Utils.getResourceIdentifier(context, name, "drawable"),
            context.theme,
        )
    }

    init {
        // Initialize circular progress view
        feedbackProgressView = CircularProgressView(
            context,
            config.overlayTextBackgroundColor,
            config.overlayTextBackgroundOnlyIcon,
            config.overlayTextSize.toFloat(), // Convert Int to Float
            config.overlayForegroundColor // Pass correct icon color
        ).apply {
            layoutParams = LayoutParams(300, 300).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            visibility = GONE
        }
        addView(feedbackProgressView)

        // Load icons
        autoBrightnessIcon = getDrawable("revanced_ic_sc_brightness_auto")
        manualBrightnessIcon = getDrawable("revanced_ic_sc_brightness_manual")
        mutedVolumeIcon = getDrawable("revanced_ic_sc_volume_mute")
        normalVolumeIcon = getDrawable("revanced_ic_sc_volume_normal")
    }

    private val feedbackHideHandler = Handler(Looper.getMainLooper())
    private val feedbackHideCallback = Runnable {
        feedbackProgressView.visibility = GONE
    }

    /**
     * Displays the circular progress indicator with the given value.
     * @param value Text to display (percentage, number, or "Auto")
     * @param progress Progress value for the circular bar
     * @param max Maximum value of the scale
     * @param icon Drawable icon to display
     * @param isBrightness If true, use brightness color; otherwise, use volume color
     */
    private fun showFeedbackView(value: String, progress: Int, max: Int, icon: Drawable, isBrightness: Boolean) {
        feedbackHideHandler.removeCallbacks(feedbackHideCallback)
        feedbackHideHandler.postDelayed(feedbackHideCallback, config.overlayShowTimeoutMillis)
        feedbackProgressView.apply {
            setProgress(progress, max, value, isBrightness)
            setIcon(icon)
            visibility = VISIBLE
        }
    }

    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        val icon = if (newVolume == 0) mutedVolumeIcon else normalVolumeIcon
        showFeedbackView("$newVolume", newVolume, maximumVolume, icon, isBrightness = false)
    }

    override fun onBrightnessChanged(brightness: Double) {
        if (config.shouldLowestValueEnableAutoBrightness && brightness <= 0) {
            showFeedbackView(str("revanced_swipe_lowest_value_enable_auto_brightness_overlay_text"),
                0, 100, autoBrightnessIcon, isBrightness = true)
        } else {
            val brightnessValue = round(brightness).toInt()
            showFeedbackView("$brightnessValue%", brightnessValue, 100, manualBrightnessIcon, isBrightness = true)
        }
    }

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
 * Custom View for rendering a circular progress indicator.
 */
class CircularProgressView @JvmOverloads constructor(
    context: Context,
    private val overlayTextBackgroundColor: Int, // Background with opacity
    private val onlyIconMode: Boolean, // If true, only icon is shown
    private val overlayTextSize: Float, // Text size from config
    private val overlayForegroundColor: Int, // User-defined color for text and icon
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = 0x33000000 // Semi-transparent outer ring
    }

    private val brightnessPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = 0xFFFFA500.toInt() // Orange for brightness
        strokeCap = Paint.Cap.ROUND
    }

    private val volumePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = 0xFF2196F3.toInt() // Blue for volume
        strokeCap = Paint.Cap.ROUND
    }

    private val innerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = overlayTextBackgroundColor // Use correct opacity
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = overlayForegroundColor // Now using overlayForegroundColor
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, overlayTextSize, resources.displayMetrics
        ) // Correct SP handling, 1.5x larger
    }

    private var progress = 0
    private var maxProgress = 100
    private var displayText: String = "0"
    private var isBrightness = true
    private var icon: Drawable? = null

    fun setProgress(value: Int, max: Int, text: String, isBrightnessMode: Boolean) {
        progress = value
        maxProgress = max
        displayText = shortenTextIfNeeded(text)
        isBrightness = isBrightnessMode
        invalidate()
    }

    fun setIcon(drawable: Drawable) {
        icon = drawable
        icon?.setTint(overlayForegroundColor) // Apply correct foreground color
        invalidate()
    }

    /**
     * Shorten text if it's too long to fit inside the ring.
     */
    private fun shortenTextIfNeeded(text: String): String {
        val maxWidth = width * 0.5f // Maximum allowed width for text
        var textToDisplay = text

        while (textPaint.measureText(textToDisplay) > maxWidth && textToDisplay.length > 4) {
            textToDisplay = textToDisplay.dropLast(1) // Remove last character
        }

        return if (textToDisplay != text) "$textToDisplay..." else textToDisplay // Add "..." if shortened
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        rectF.set(20f, 20f, size - 20f, size - 20f)

        canvas.drawOval(rectF, backgroundPaint)
        canvas.drawCircle(width / 2f, height / 2f, size / 3, innerBackgroundPaint)

        val paint = if (isBrightness) brightnessPaint else volumePaint
        val sweepAngle = (progress.toFloat() / maxProgress) * 360
        canvas.drawArc(rectF, -90f, sweepAngle, false, paint)

        icon?.let {
            val iconSize = 80
            val iconX = (width - iconSize) / 2
            val iconY = (height / 2) - if (onlyIconMode) 40 else 90
            it.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
            it.draw(canvas)
        }

        if (!onlyIconMode) {
            canvas.drawText(displayText, width / 2f, height / 2f + 55f, textPaint)
        }
    }

    private val rectF = RectF()
}
