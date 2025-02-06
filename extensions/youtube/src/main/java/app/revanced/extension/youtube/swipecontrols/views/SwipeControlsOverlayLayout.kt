package app.revanced.extension.youtube.swipecontrols.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
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
    /**
     * DO NOT use this, for tools only
     */
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
        ).apply {
            setTint(config.overlayForegroundColor)
        }
    }

    init {
        // Initialize circular progress view
        feedbackProgressView = CircularProgressView(context, config.overlayForegroundColor).apply {
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
            setIcon(icon) // Set correct icon
            visibility = VISIBLE
        }
    }

    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        // Use muted icon when volume is 0, otherwise normal icon
        val icon = if (newVolume == 0) mutedVolumeIcon else normalVolumeIcon
        showFeedbackView("$newVolume", newVolume, maximumVolume, icon, isBrightness = false)
    }

    override fun onBrightnessChanged(brightness: Double) {
        if (config.shouldLowestValueEnableAutoBrightness && brightness <= 0) {
            // Show "Auto" when brightness is at the lowest level
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
    private val overlayDarkness: Int, // Darkness percentage (0-100)
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

    // Compute inner background darkness based on overlayDarkness percentage
    private val innerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        val alpha = (overlayDarkness * 2.55).toInt() // Convert 0-100% to 0-255 alpha
        color = (alpha shl 24) or 0x000000 // Apply transparency to black color
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 60f
        color = 0xFFFFFFFF.toInt() // White text
        textAlign = Paint.Align.CENTER
    }

    private var progress = 0
    private var maxProgress = 100
    private var displayText: String = "0"
    private var isBrightness = true
    private var icon: Drawable? = null

    fun setProgress(value: Int, max: Int, text: String, isBrightnessMode: Boolean) {
        progress = value
        maxProgress = max
        displayText = text
        isBrightness = isBrightnessMode
        invalidate()
    }

    fun setIcon(drawable: Drawable) {
        icon = drawable
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        rectF.set(20f, 20f, size - 20f, size - 20f)

        // Draw background circle
        canvas.drawOval(rectF, backgroundPaint)

        // Draw inner darkened background (based on overlayDarkness %)
        val innerRadius = size / 3
        canvas.drawCircle(width / 2f, height / 2f, innerRadius, innerBackgroundPaint)

        // Choose correct paint for brightness or volume
        val paint = if (isBrightness) brightnessPaint else volumePaint

        // Draw progress arc
        val sweepAngle = (progress.toFloat() / maxProgress) * 360
        canvas.drawArc(rectF, -90f, sweepAngle, false, paint)

        // Draw icon above text (slightly lower than before)
        icon?.let {
            val iconSize = 80
            val iconX = (width - iconSize) / 2
            val iconY = (height / 2) - 90 // Slightly lowered
            it.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
            it.draw(canvas)
        }

        // Draw text below the icon (slightly lower)
        canvas.drawText(displayText, width / 2f, height / 2f + 55f, textPaint) // Lowered text
    }

    private val rectF = RectF()
}
