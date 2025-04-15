package app.revanced.extension.youtube.swipecontrols.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
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
import kotlin.math.max
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
        val layoutWidth = (screenWidth * 4 / 5).toInt() // Cap at ~360dp
        horizontalProgressView = HorizontalProgressView(
            context,
            config.overlayBackgroundOpacity,
            config.overlayShowOverlayMinimalStyle,
            config.overlayProgressColor,
            config.overlayFillBackgroundPaint,
            config.overlayTextColor
        ).apply {
            layoutParams = LayoutParams(layoutWidth, dpToPx(context, 32f).toInt()).apply {
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

    // Rect for text measurement
    protected val textBounds = Rect()

    protected var progress = 0
    protected var maxProgress = 100
    protected var displayText: String = "0"
    protected var isBrightness = true
    var icon: Drawable? = null

    open fun setProgress(value: Int, max: Int, text: String, isBrightnessMode: Boolean) {
        progress = value
        maxProgress = max
        displayText = text
        isBrightness = isBrightnessMode
        invalidate()
    }

    protected fun measureTextWidth(text: String, paint: Paint): Int {
        paint.getTextBounds(text, 0, text.length, textBounds)
        return textBounds.width()
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

    override fun setProgress(value: Int, max: Int, text: String, isBrightnessMode: Boolean) {
        super.setProgress(value, max, text, isBrightnessMode)
        requestLayout()
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
    private var textWidth = 0f
    private val progressBarHeight = dpToPx(context, 3f)
    private val progressBarWidth: Float = resources.displayMetrics.widthPixels / 4f

    init {
        textPaint.textSize = dpToPx(context, 14f)
        progressPaint.strokeWidth = 0f
        progressPaint.strokeCap = Paint.Cap.BUTT
        progressPaint.style = Paint.Style.FILL
        fillBackgroundPaint.style = Paint.Style.FILL
    }

    /**
     * Calculate required width based on content
     * @return Required width to display all elements
     */
    private fun calculateRequiredWidth(): Float {
        textWidth = measureTextWidth(displayText, textPaint).toFloat()

        return if (!overlayShowOverlayMinimalStyle) {
            padding + iconSize + padding + progressBarWidth + padding + textWidth + padding
        } else {
            padding + iconSize + padding + textWidth + padding
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val suggestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val suggestedHeight = MeasureSpec.getSize(heightMeasureSpec)

        val height = suggestedHeight
        val requiredWidth = calculateRequiredWidth().toInt()
        val width = min(max(100, requiredWidth), suggestedWidth)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        textWidth = measureTextWidth(displayText, textPaint).toFloat()

        val cornerRadius = viewHeight / 2

        val startX = padding
        val iconEndX = startX + iconSize

        val textStartX = (viewWidth - 1.5 * padding - textWidth).toFloat()

        canvas.drawRoundRect(
            0f, 0f, viewWidth, viewHeight,
            cornerRadius, cornerRadius, backgroundPaint
        )

        icon?.let {
            val iconY = viewHeight / 2 - iconSize / 2
            it.setBounds(
                startX.toInt(),
                iconY.toInt(),
                (startX + iconSize).toInt(),
                (iconY + iconSize).toInt()
            )
            it.draw(canvas)
        }

        val textY = viewHeight / 2 + textPaint.textSize / 3
        textPaint.textAlign = Paint.Align.LEFT

        if (overlayShowOverlayMinimalStyle) {
            canvas.drawText(displayText, textStartX, textY, textPaint)
        } else {
            val progressStartX = iconEndX + padding
            val progressEndX = textStartX - padding
            val progressWidth = progressEndX - progressStartX

            if (progressWidth > 50) {
                canvas.drawRoundRect(
                    progressStartX,
                    viewHeight / 2 - progressBarHeight / 2,
                    progressEndX,
                    viewHeight / 2 + progressBarHeight / 2,
                    progressBarHeight / 2,
                    progressBarHeight / 2,
                    fillBackgroundPaint
                )
                val progressValue = (progress.toFloat() / maxProgress) * progressWidth
                canvas.drawRoundRect(
                    progressStartX,
                    viewHeight / 2 - progressBarHeight / 2,
                    progressStartX + progressValue,
                    viewHeight / 2 + progressBarHeight / 2,
                    progressBarHeight / 2,
                    progressBarHeight / 2,
                    progressPaint
                )
            }
            canvas.drawText(displayText, textStartX, textY, textPaint)
        }
    }

    override fun setProgress(value: Int, max: Int, text: String, isBrightnessMode: Boolean) {
        super.setProgress(value, max, text, isBrightnessMode)
        requestLayout()
    }
}