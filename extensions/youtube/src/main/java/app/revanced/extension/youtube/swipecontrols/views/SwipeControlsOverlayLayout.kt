package app.revanced.extension.youtube.swipecontrols.views

import android.annotation.SuppressLint
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
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.swipecontrols.SwipeControlsConfigurationProvider
import app.revanced.extension.youtube.swipecontrols.misc.SwipeControlsOverlay
import kotlin.math.min
import kotlin.math.round

/**
 * Utility function to convert dp to pixels based on device density.
 */
fun dpToPx(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
}

/**
 * Main overlay layout for displaying volume and brightness level with both circular and horizontal progress bars.
 */
class SwipeControlsOverlayLayout(
    context: Context,
    private val config: SwipeControlsConfigurationProvider,
) : RelativeLayout(context), SwipeControlsOverlay {

    constructor(context: Context) : this(context, SwipeControlsConfigurationProvider())

    // Drawable icons for brightness and volume
    private val autoBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_auto")
    private val lowBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_low")
    private val mediumBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_medium")
    private val highBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_high")
    private val fullBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_full")
    private val mutedVolumeIcon: Drawable = getDrawable("revanced_ic_sc_volume_mute")
    private val lowVolumeIcon: Drawable = getDrawable("revanced_ic_sc_volume_low")
    private val normalVolumeIcon: Drawable = getDrawable("revanced_ic_sc_volume_normal")
    private val fullVolumeIcon: Drawable = getDrawable("revanced_ic_sc_volume_high")

    // Function to retrieve drawable resources by name
    private fun getDrawable(name: String): Drawable {
        val drawable = resources.getDrawable(
            Utils.getResourceIdentifier(context, name, "drawable"),
            context.theme,
        )
        drawable.setTint(config.overlayTextColor)
        return drawable
    }

    // Initialize progress bars
    private val circularProgressView: CircularProgressView
    private val horizontalProgressView: HorizontalProgressView

    init {
        // Initialize circular progress bar
        circularProgressView = CircularProgressView(
            context,
            config.overlayBackgroundOpacity,
            config.overlayShowOverlayMinimalStyle,
            config.overlayProgressColor,
            config.overlayFillBackgroundPaint,
            config.overlayTextColor
        ).apply {
            layoutParams = LayoutParams(dpToPx(context, 100f).toInt(), dpToPx(context, 100f).toInt()).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            visibility = GONE // Initially hidden
        }
        addView(circularProgressView)

        // Initialize horizontal progress bar
        val screenWidth = resources.displayMetrics.widthPixels
        val layoutWidth = min((screenWidth * 2 / 3).toInt(), dpToPx(context, 360f).toInt()) // Cap at ~360dp
        horizontalProgressView = HorizontalProgressView(
            context,
            config.overlayBackgroundOpacity,
            config.overlayShowOverlayMinimalStyle,
            config.overlayProgressColor,
            config.overlayFillBackgroundPaint,
            config.overlayTextColor
        ).apply {
            layoutParams = LayoutParams(layoutWidth, dpToPx(context, 30f).toInt()).apply {
                addRule(CENTER_HORIZONTAL)
                topMargin = dpToPx(context, 10f).toInt()
            }
            visibility = GONE // Initially hidden
        }
        addView(horizontalProgressView)
    }

    // Handler and callback for hiding progress bars
    private val feedbackHideHandler = Handler(Looper.getMainLooper())
    private val feedbackHideCallback = Runnable {
        circularProgressView.visibility = GONE
        horizontalProgressView.visibility = GONE
    }

    /**
     * Displays the progress bar with the appropriate value, icon, and type (brightness or volume).
     */
    private fun showFeedbackView(value: String, progress: Int, max: Int, icon: Drawable, isBrightness: Boolean) {
        feedbackHideHandler.removeCallbacks(feedbackHideCallback)
        feedbackHideHandler.postDelayed(feedbackHideCallback, config.overlayShowTimeoutMillis)

        val viewToShow = if (config.isCircularProgressBar) circularProgressView else horizontalProgressView
        viewToShow.apply {
            setProgress(progress, max, value, isBrightness)
            this.icon = icon
            visibility = VISIBLE
        }
    }

    // Handle volume change
    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        val volumePercentage = (newVolume.toFloat() / maximumVolume) * 100
        val icon = when {
            newVolume == 0 -> mutedVolumeIcon
            volumePercentage < 33 -> lowVolumeIcon
            volumePercentage < 66 -> normalVolumeIcon
            else -> fullVolumeIcon
        }
        showFeedbackView("$newVolume", newVolume, maximumVolume, icon, isBrightness = false)
    }

    // Handle brightness change
    override fun onBrightnessChanged(brightness: Double) {
        if (config.shouldLowestValueEnableAutoBrightness && brightness <= 0) {
            showFeedbackView("Auto", 0, 100, autoBrightnessIcon, isBrightness = true)
        } else {
            val brightnessValue = round(brightness).toInt()
            val icon = when {
                brightnessValue < 25 -> lowBrightnessIcon
                brightnessValue < 50 -> mediumBrightnessIcon
                brightnessValue < 75 -> highBrightnessIcon
                else -> fullBrightnessIcon
            }
            showFeedbackView("$brightnessValue%", brightnessValue, 100, icon, isBrightness = true)
        }
    }

    // Begin swipe session
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
 * Abstract base class for progress views.
 */
abstract class AbstractProgressView(
    context: Context,
    overlayBackgroundOpacity: Int,
    protected val overlayShowOverlayMinimalStyle: Boolean,
    overlayProgressColor: Int,
    overlayFillBackgroundPaint: Int,
    private val overlayTextColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Combined paint creation function for both fill and stroke styles
    private fun createPaint(color: Int, style: Paint.Style = Paint.Style.FILL, strokeCap: Paint.Cap = Paint.Cap.BUTT, strokeWidth: Float = 0f) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = style
        this.color = color
        this.strokeCap = strokeCap
        this.strokeWidth = strokeWidth
    }

    // Initialize paints
    val backgroundPaint     = createPaint(overlayBackgroundOpacity,   style = Paint.Style.FILL)
    val progressPaint       = createPaint(overlayProgressColor,       style = Paint.Style.STROKE, strokeCap = Paint.Cap.ROUND, strokeWidth = dpToPx(context, 6f))
    val fillBackgroundPaint = createPaint(overlayFillBackgroundPaint, style = Paint.Style.FILL)
    val textPaint           = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = overlayTextColor
        textAlign = Paint.Align.CENTER
        textSize  = dpToPx(context, 14f)
    }

    protected var progress = 0
    protected var maxProgress = 100
    protected var displayText: String = "0"
    protected var isBrightness = true
    var icon: Drawable? = null

    fun setProgress(value: Int, max: Int, text: String, isBrightnessMode: Boolean) {
        progress = value
        maxProgress = max
        displayText = text
        isBrightness = isBrightnessMode
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // Base class implementation can be empty
    }
}

/**
 * Custom view for rendering a circular progress indicator with icons and text.
 */
@SuppressLint("ViewConstructor")
class CircularProgressView(
    context: Context,
    overlayBackgroundOpacity: Int,
    overlayShowOverlayMinimalStyle: Boolean,
    overlayProgressColor: Int,
    overlayFillBackgroundPaint: Int,
    overlayTextColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractProgressView(
    context,
    overlayBackgroundOpacity,
    overlayShowOverlayMinimalStyle,
    overlayProgressColor,
    overlayFillBackgroundPaint,
    overlayTextColor,
    attrs,
    defStyleAttr
) {
    private val rectF = RectF()

    init {
        textPaint.textSize = dpToPx(context, 14f)
        progressPaint.strokeWidth = dpToPx(context, 6f)
        fillBackgroundPaint.strokeWidth = dpToPx(context, 6f)
        progressPaint.strokeCap = Paint.Cap.ROUND
        fillBackgroundPaint.strokeCap = Paint.Cap.BUTT
        progressPaint.style = Paint.Style.STROKE
        fillBackgroundPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val inset = dpToPx(context, 6f)
        rectF.set(inset, inset, size - inset, size - inset)

        canvas.drawOval(rectF, fillBackgroundPaint) // Draw the outer ring.
        canvas.drawCircle(width / 2f, height / 2f, size / 3, backgroundPaint) // Draw the inner circle.

        // Select the paint for drawing based on whether it's brightness or volume.
        val sweepAngle = (progress.toFloat() / maxProgress) * 360
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint) // Draw the progress arc.

        // Draw the icon in the center.
        icon?.let {
            val iconSize = dpToPx(context, if (overlayShowOverlayMinimalStyle) 36f else 24f).toInt()
            val iconX = (width - iconSize) / 2
            val iconY = if (overlayShowOverlayMinimalStyle) {
                (height - iconSize) / 2
            } else {
                (height / 2) - dpToPx(context, 24f).toInt()
            }
            it.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
            it.draw(canvas)
        }

        // If not a minimal style mode, draw the text inside the ring.
        if (!overlayShowOverlayMinimalStyle) {
            canvas.drawText(displayText, width / 2f, height / 2f + dpToPx(context, 20f), textPaint)
        }
    }
}

/**
 * Custom view for rendering a rectangular progress bar with icons and text.
 */
@SuppressLint("ViewConstructor")
class HorizontalProgressView(
    context: Context,
    overlayBackgroundOpacity: Int,
    overlayShowOverlayMinimalStyle: Boolean,
    overlayProgressColor: Int,
    overlayFillBackgroundPaint: Int,
    overlayTextColor: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractProgressView(
    context,
    overlayBackgroundOpacity,
    overlayShowOverlayMinimalStyle,
    overlayProgressColor,
    overlayFillBackgroundPaint,
    overlayTextColor,
    attrs,
    defStyleAttr
) {

    private val iconSize = dpToPx(context, 20f)
    private val padding = dpToPx(context, 12f)

    init {
        textPaint.textSize = dpToPx(context, 14f)
        progressPaint.strokeWidth = 0f
        progressPaint.strokeCap = Paint.Cap.BUTT
        progressPaint.style = Paint.Style.FILL
        fillBackgroundPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Radius for rounded corners
        val cornerRadius = min(width, height) / 2

        // Calculate the total width for the elements
        val minimalElementWidth = if (isBrightness) {
            6 * padding + iconSize // Add space for brightness
        } else {
            5 * padding + iconSize // No extra space for volume
        }

        // Calculate the starting point (X) to center the elements
        val minimalStartX = (width - minimalElementWidth) / 2

        // Draw the background
        if (!overlayShowOverlayMinimalStyle) {
            canvas.drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, backgroundPaint)
        } else {
            canvas.drawRoundRect(minimalStartX, 0f, minimalStartX + minimalElementWidth, height, cornerRadius, cornerRadius, backgroundPaint)
        }

        if (!overlayShowOverlayMinimalStyle) {
            // Draw the fill background
            val startX = 2 * padding + iconSize
            val endX = if (isBrightness) {
                width - 4.5f * padding // Reduce padding for brightness
            } else {
                width - 4 * padding // Standard padding for volume
            }
            val fillWidth = endX - startX

            canvas.drawRoundRect(
                startX,
                height / 2 - dpToPx(context, 1.5f),
                endX,
                height / 2 + dpToPx(context, 1.5f),
                dpToPx(context, 3f), dpToPx(context, 3f),
                fillBackgroundPaint
            )

            // Draw the progress
            val progressWidth = (progress.toFloat() / maxProgress) * fillWidth
            canvas.drawRoundRect(
                startX,
                height / 2 - dpToPx(context, 1.5f),
                startX + progressWidth,
                height / 2 + dpToPx(context, 1.5f),
                dpToPx(context, 3f), dpToPx(context, 3f),
                progressPaint
            )
        }

        // Draw the icon
        icon?.let {
            val iconX = if (!overlayShowOverlayMinimalStyle) padding else padding + minimalStartX
            val iconY = height / 2 - iconSize / 2
            it.setBounds(iconX.toInt(), iconY.toInt(), (iconX + iconSize).toInt(), (iconY + iconSize).toInt())
            it.draw(canvas)
        }

        // Draw the text on the right with conditional padding
        val textX = if (!overlayShowOverlayMinimalStyle) {
            if (isBrightness) width - 2.5f * padding else width - 2 * padding // Reduce padding for brightness
        } else {
            minimalStartX + minimalElementWidth - 2 * padding
        }
        val textY = height / 2 + textPaint.textSize / 3

        // Draw the text
        canvas.drawText(displayText, textX, textY, textPaint)
    }
}