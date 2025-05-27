package app.revanced.extension.youtube.swipecontrols.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
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
import app.revanced.extension.shared.StringRef.str
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.swipecontrols.SwipeControlsConfigurationProvider
import app.revanced.extension.youtube.swipecontrols.misc.SwipeControlsOverlay
import kotlin.math.min
import kotlin.math.max
import kotlin.math.round

/**
 * Convert dp to pixels based on system display density.
 */
fun Float.toDisplayPixels(): Float {
    return this * Resources.getSystem().displayMetrics.density
}

/**
 * Main overlay layout for displaying volume and brightness level with circular, horizontal and vertical progress bars.
 */
class SwipeControlsOverlayLayout(
    context: Context,
    private val config: SwipeControlsConfigurationProvider,
) : RelativeLayout(context), SwipeControlsOverlay {

    constructor(context: Context) : this(context, SwipeControlsConfigurationProvider())

    // Drawable icons for brightness and volume.
    private val autoBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_auto")
    private val lowBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_low")
    private val mediumBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_medium")
    private val highBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_high")
    private val fullBrightnessIcon: Drawable = getDrawable("revanced_ic_sc_brightness_full")
    private val mutedVolumeIcon: Drawable = getDrawable("revanced_ic_sc_volume_mute")
    private val lowVolumeIcon: Drawable = getDrawable("revanced_ic_sc_volume_low")
    private val normalVolumeIcon: Drawable = getDrawable("revanced_ic_sc_volume_normal")
    private val fullVolumeIcon: Drawable = getDrawable("revanced_ic_sc_volume_high")

    // Function to retrieve drawable resources by name.
    private fun getDrawable(name: String): Drawable {
        val drawable = resources.getDrawable(
            Utils.getResourceIdentifier(context, name, "drawable"),
            context.theme,
        )
        drawable.setTint(config.overlayTextColor)
        return drawable
    }

    // Initialize progress bars.
    private val circularProgressView: CircularProgressView
    private val horizontalProgressView: HorizontalProgressView
    private val verticalBrightnessProgressView: VerticalProgressView
    private val verticalVolumeProgressView: VerticalProgressView

    init {
        // Initialize circular progress bar.
        circularProgressView = CircularProgressView(
            context,
            config.overlayBackgroundOpacity,
            config.overlayStyle.isMinimal,
            config.overlayBrightnessProgressColor, // Placeholder, updated in showFeedbackView.
            config.overlayFillBackgroundPaint,
            config.overlayTextColor,
            config.overlayTextSize
        ).apply {
            layoutParams = LayoutParams(100f.toDisplayPixels().toInt(), 100f.toDisplayPixels().toInt()).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            visibility = GONE // Initially hidden.
        }
        addView(circularProgressView)

        // Initialize horizontal progress bar.
        val screenWidth = resources.displayMetrics.widthPixels
        val layoutWidth = (screenWidth * 4 / 5).toInt() // Cap at ~360dp.
        horizontalProgressView = HorizontalProgressView(
            context,
            config.overlayBackgroundOpacity,
            config.overlayStyle.isMinimal,
            config.overlayBrightnessProgressColor, // Placeholder, updated in showFeedbackView.
            config.overlayFillBackgroundPaint,
            config.overlayTextColor,
            config.overlayTextSize
        ).apply {
            layoutParams = LayoutParams(layoutWidth, 32f.toDisplayPixels().toInt()).apply {
                addRule(CENTER_HORIZONTAL)
                if (config.overlayStyle.isHorizontalMinimalCenter) {
                    addRule(CENTER_VERTICAL)
                } else {
                    topMargin = 20f.toDisplayPixels().toInt()
                }
            }
            visibility = GONE // Initially hidden.
        }
        addView(horizontalProgressView)

        // Initialize vertical progress bar for brightness (right side).
        verticalBrightnessProgressView = VerticalProgressView(
            context,
            config.overlayBackgroundOpacity,
            config.overlayStyle.isMinimal,
            config.overlayBrightnessProgressColor,
            config.overlayFillBackgroundPaint,
            config.overlayTextColor,
            config.overlayTextSize
        ).apply {
            layoutParams = LayoutParams(40f.toDisplayPixels().toInt(), 150f.toDisplayPixels().toInt()).apply {
                addRule(ALIGN_PARENT_RIGHT)
                rightMargin = 40f.toDisplayPixels().toInt()
                addRule(CENTER_VERTICAL)
            }
            visibility = GONE // Initially hidden.
        }
        addView(verticalBrightnessProgressView)

        // Initialize vertical progress bar for volume (left side).
        verticalVolumeProgressView = VerticalProgressView(
            context,
            config.overlayBackgroundOpacity,
            config.overlayStyle.isMinimal,
            config.overlayVolumeProgressColor,
            config.overlayFillBackgroundPaint,
            config.overlayTextColor,
            config.overlayTextSize
        ).apply {
            layoutParams = LayoutParams(40f.toDisplayPixels().toInt(), 150f.toDisplayPixels().toInt()).apply {
                addRule(ALIGN_PARENT_LEFT)
                leftMargin = 40f.toDisplayPixels().toInt()
                addRule(CENTER_VERTICAL)
            }
            visibility = GONE // Initially hidden.
        }
        addView(verticalVolumeProgressView)
    }

    // Handler and callback for hiding progress bars.
    private val feedbackHideHandler = Handler(Looper.getMainLooper())
    private val feedbackHideCallback = Runnable {
        circularProgressView.visibility = GONE
        horizontalProgressView.visibility = GONE
        verticalBrightnessProgressView.visibility = GONE
        verticalVolumeProgressView.visibility = GONE
    }

    /**
     * Displays the progress bar with the appropriate value, icon, and type (brightness or volume).
     */
    private fun showFeedbackView(value: String, progress: Int, max: Int, icon: Drawable, isBrightness: Boolean) {
        feedbackHideHandler.removeCallbacks(feedbackHideCallback)
        feedbackHideHandler.postDelayed(feedbackHideCallback, config.overlayShowTimeoutMillis)

        val viewToShow = when {
            config.overlayStyle.isCircular -> circularProgressView
            config.overlayStyle.isVertical ->
                if (isBrightness)
                    verticalBrightnessProgressView
                else
                    verticalVolumeProgressView
            else -> horizontalProgressView
        }
        viewToShow.apply {
            // Set the appropriate progress color.
            if (this is CircularProgressView || this is HorizontalProgressView) {
                setProgressColor(
                    if (isBrightness)
                        config.overlayBrightnessProgressColor
                    else
                        config.overlayVolumeProgressColor
                )
            }
            setProgress(progress, max, value, isBrightness)
            this.icon = icon
            visibility = VISIBLE
        }
    }

    // Handle volume change.
    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        val volumePercentage = (newVolume.toFloat() / maximumVolume) * 100
        val icon = when {
            newVolume == 0 -> mutedVolumeIcon
            volumePercentage < 25 -> lowVolumeIcon
            volumePercentage < 50 -> normalVolumeIcon
            else -> fullVolumeIcon
        }
        showFeedbackView("$newVolume", newVolume, maximumVolume, icon, isBrightness = false)
    }

    // Handle brightness change.
    override fun onBrightnessChanged(brightness: Double) {
        if (config.shouldLowestValueEnableAutoBrightness && brightness <= 0) {
            val displayText = if (config.overlayStyle.isVertical) "А"
            else str("revanced_swipe_lowest_value_enable_auto_brightness_overlay_text")
            showFeedbackView(displayText, 0, 100, autoBrightnessIcon, isBrightness = true)
        } else {
            val brightnessValue = round(brightness).toInt()
            val clampedProgress = max(0, brightnessValue)
            val icon = when {
                clampedProgress < 25 -> lowBrightnessIcon
                clampedProgress < 50 -> mediumBrightnessIcon
                clampedProgress < 75 -> highBrightnessIcon
                else -> fullBrightnessIcon
            }
            val displayText = if (config.overlayStyle.isVertical) "$clampedProgress" else "$clampedProgress%"
            showFeedbackView(displayText, clampedProgress, 100, icon, isBrightness = true)
        }
    }

    // Begin swipe session.
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
    protected val isMinimalStyle: Boolean,
    overlayProgressColor: Int,
    overlayFillBackgroundPaint: Int,
    private val overlayTextColor: Int,
    protected val overlayTextSize: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Combined paint creation function for both fill and stroke styles.
    private fun createPaint(
        color: Int,
        style: Paint.Style = Paint.Style.FILL,
        strokeCap: Paint.Cap = Paint.Cap.BUTT,
        strokeWidth: Float = 0f
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = style
        this.color = color
        this.strokeCap = strokeCap
        this.strokeWidth = strokeWidth
    }

    // Initialize paints.
    val backgroundPaint = createPaint(
        overlayBackgroundOpacity,
        style = Paint.Style.FILL
    )
    val progressPaint = createPaint(
        overlayProgressColor,
        style = Paint.Style.STROKE,
        strokeCap = Paint.Cap.ROUND,
        strokeWidth = 6f.toDisplayPixels()
    )
    val fillBackgroundPaint = createPaint(
        overlayFillBackgroundPaint,
        style = Paint.Style.FILL
    )
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = overlayTextColor
        textAlign = Paint.Align.CENTER
        textSize = overlayTextSize.toFloat().toDisplayPixels()
    }

    // Rect for text measurement.
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

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    protected fun measureTextWidth(text: String, paint: Paint): Int {
        paint.getTextBounds(text, 0, text.length, textBounds)
        return textBounds.width()
    }

    override fun onDraw(canvas: Canvas) {
        // Base class implementation can be empty.
    }
}

/**
 * Custom view for rendering a circular progress indicator with icons and text.
 */
@SuppressLint("ViewConstructor")
class CircularProgressView(
    context: Context,
    overlayBackgroundOpacity: Int,
    isMinimalStyle: Boolean,
    overlayProgressColor: Int,
    overlayFillBackgroundPaint: Int,
    overlayTextColor: Int,
    overlayTextSize: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractProgressView(
    context,
    overlayBackgroundOpacity,
    isMinimalStyle,
    overlayProgressColor,
    overlayFillBackgroundPaint,
    overlayTextColor,
    overlayTextSize,
    attrs,
    defStyleAttr
) {
    private val rectF = RectF()

    init {
        progressPaint.strokeWidth = 6f.toDisplayPixels()
        fillBackgroundPaint.strokeWidth = 6f.toDisplayPixels()
        progressPaint.strokeCap = Paint.Cap.ROUND
        fillBackgroundPaint.strokeCap = Paint.Cap.BUTT
        progressPaint.style = Paint.Style.STROKE
        fillBackgroundPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val inset = 6f.toDisplayPixels()
        rectF.set(inset, inset, size - inset, size - inset)

        canvas.drawOval(rectF, fillBackgroundPaint) // Draw the outer ring.
        canvas.drawCircle(width / 2f, height / 2f, size / 3, backgroundPaint) // Draw the inner circle.

        // Select the paint for drawing based on whether it's brightness or volume.
        val sweepAngle = (progress.toFloat() / maxProgress) * 360
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint) // Draw the progress arc.

        // Draw the icon in the center.
        icon?.let {
            val iconSize = (if (isMinimalStyle) 36f else 24f).toDisplayPixels().toInt()
            val iconX = (width - iconSize) / 2
            val iconY = if (isMinimalStyle) {
                (height - iconSize) / 2
            } else {
                (height / 2) - 24f.toDisplayPixels().toInt()
            }
            it.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
            it.draw(canvas)
        }

        // If not a minimal style mode, draw the text inside the ring.
        if (!isMinimalStyle) {
            canvas.drawText(displayText, width / 2f, height / 2f + 20f.toDisplayPixels(), textPaint)
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
    isMinimalStyle: Boolean,
    overlayProgressColor: Int,
    overlayFillBackgroundPaint: Int,
    overlayTextColor: Int,
    overlayTextSize: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractProgressView(
    context,
    overlayBackgroundOpacity,
    isMinimalStyle,
    overlayProgressColor,
    overlayFillBackgroundPaint,
    overlayTextColor,
    overlayTextSize,
    attrs,
    defStyleAttr
) {

    private val iconSize = 20f.toDisplayPixels()
    private val padding = 12f.toDisplayPixels()
    private var textWidth = 0f
    private val progressBarHeight = 3f.toDisplayPixels()
    private val progressBarWidth: Float = resources.displayMetrics.widthPixels / 4f

    init {
        progressPaint.strokeWidth = 0f
        progressPaint.strokeCap = Paint.Cap.BUTT
        progressPaint.style = Paint.Style.FILL
        fillBackgroundPaint.style = Paint.Style.FILL
    }

    /**
     * Calculate required width based on content.
     * @return Required width to display all elements.
     */
    private fun calculateRequiredWidth(): Float {
        textWidth = measureTextWidth(displayText, textPaint).toFloat()

        return if (!isMinimalStyle) {
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
        val viewHeightHalf = viewHeight / 2

        textWidth = measureTextWidth(displayText, textPaint).toFloat()

        val cornerRadius = viewHeightHalf

        val startX = padding
        val iconEndX = startX + iconSize

        val textStartX = (viewWidth - 1.5f * padding - textWidth)

        canvas.drawRoundRect(
            0f, 0f, viewWidth, viewHeight,
            cornerRadius, cornerRadius, backgroundPaint
        )

        icon?.let {
            val iconY = viewHeightHalf - iconSize / 2
            it.setBounds(
                startX.toInt(),
                iconY.toInt(),
                (startX + iconSize).toInt(),
                (iconY + iconSize).toInt()
            )
            it.draw(canvas)
        }

        val textY = viewHeightHalf + textPaint.textSize / 3
        textPaint.textAlign = Paint.Align.LEFT

        if (isMinimalStyle) {
            canvas.drawText(displayText, textStartX, textY, textPaint)
        } else {
            val progressStartX = iconEndX + padding
            val progressEndX = textStartX - padding
            val progressWidth = progressEndX - progressStartX

            if (progressWidth > 50) {
                val progressBarHeightHalf = progressBarHeight / 2.0f
                val viewHeightHalfMinusProgressBarHeightHalf = viewHeightHalf - progressBarHeightHalf
                val viewHeightHalfPlusProgressBarHeightHalf = viewHeightHalf + progressBarHeightHalf

                canvas.drawRoundRect(
                    progressStartX,
                    viewHeightHalfMinusProgressBarHeightHalf,
                    progressEndX,
                    viewHeightHalfPlusProgressBarHeightHalf,
                    progressBarHeightHalf,
                    progressBarHeightHalf,
                    fillBackgroundPaint
                )

                val progressValue = (progress.toFloat() / maxProgress) * progressWidth
                canvas.drawRoundRect(
                    progressStartX,
                    viewHeightHalfMinusProgressBarHeightHalf,
                    progressStartX + progressValue,
                    viewHeightHalfPlusProgressBarHeightHalf,
                    progressBarHeightHalf,
                    progressBarHeightHalf,
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

/**
 * Custom view for rendering a vertical progress bar with icons and text.
 */
@SuppressLint("ViewConstructor")
class VerticalProgressView(
    context: Context,
    overlayBackgroundOpacity: Int,
    isMinimalStyle: Boolean,
    overlayProgressColor: Int,
    overlayFillBackgroundPaint: Int,
    overlayTextColor: Int,
    overlayTextSize: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractProgressView(
    context,
    overlayBackgroundOpacity,
    isMinimalStyle,
    overlayProgressColor,
    overlayFillBackgroundPaint,
    overlayTextColor,
    overlayTextSize,
    attrs,
    defStyleAttr
) {

    private val iconSize = 20f.toDisplayPixels()
    private val padding = 12f.toDisplayPixels()
    private val progressBarWidth = 3f.toDisplayPixels()
    private val progressBarHeight: Float = resources.displayMetrics.widthPixels / 3f

    init {
        progressPaint.strokeWidth = 0f
        progressPaint.strokeCap = Paint.Cap.BUTT
        progressPaint.style = Paint.Style.FILL
        fillBackgroundPaint.style = Paint.Style.FILL
    }

    /**
     * Calculate required height based on content.
     * @return Required height to display all elements.
     */
    private fun calculateRequiredHeight(): Float {
        return if (!isMinimalStyle) {
            padding + iconSize + padding + progressBarHeight + padding + textPaint.textSize + padding
        } else {
            padding + iconSize + padding + textPaint.textSize + padding
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val suggestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val suggestedHeight = MeasureSpec.getSize(heightMeasureSpec)

        val requiredHeight = calculateRequiredHeight().toInt()
        val height = min(max(100, requiredHeight), suggestedHeight)

        setMeasuredDimension(suggestedWidth, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val viewWidthHalf = viewWidth / 2
        val cornerRadius = viewWidthHalf

        val startY = padding
        val iconEndY = startY + iconSize

        val textStartY = viewHeight - padding - textPaint.textSize / 2

        canvas.drawRoundRect(
            0f, 0f, viewWidth, viewHeight,
            cornerRadius, cornerRadius, backgroundPaint
        )

        icon?.let {
            val iconX = viewWidthHalf - iconSize / 2
            it.setBounds(
                iconX.toInt(),
                startY.toInt(),
                (iconX + iconSize).toInt(),
                (startY + iconSize).toInt()
            )
            it.draw(canvas)
        }

        val textX = viewWidthHalf
        textPaint.textAlign = Paint.Align.CENTER

        if (isMinimalStyle) {
            canvas.drawText(displayText, textX, textStartY, textPaint)
        } else {
            val progressStartY = (iconEndY + padding).toFloat()
            val progressEndY = textStartY - textPaint.textSize - padding
            val progressHeight = progressEndY - progressStartY

            if (progressHeight > 50) {
                val progressBarWidthHalf = progressBarWidth / 2
                val viewHeightHalfMinusProgressBarHeightHalf = viewWidthHalf - progressBarWidthHalf
                val viewHeightHalfPlusProgressBarHeightHalf = viewWidthHalf + progressBarWidthHalf

                canvas.drawRoundRect(
                    viewHeightHalfMinusProgressBarHeightHalf,
                    progressStartY,
                    viewHeightHalfPlusProgressBarHeightHalf,
                    progressEndY,
                    progressBarWidthHalf,
                    progressBarWidthHalf,
                    fillBackgroundPaint
                )

                val progressValue = (progress.toFloat() / maxProgress) * progressHeight
                canvas.drawRoundRect(
                    viewHeightHalfMinusProgressBarHeightHalf,
                    progressEndY - progressValue,
                    viewHeightHalfPlusProgressBarHeightHalf,
                    progressEndY,
                    progressBarWidthHalf,
                    progressBarWidthHalf,
                    progressPaint
                )
            }
            canvas.drawText(displayText, textX, textStartY, textPaint)
        }
    }

    override fun setProgress(value: Int, max: Int, text: String, isBrightnessMode: Boolean) {
        super.setProgress(value, max, text, isBrightnessMode)
        requestLayout()
    }
}
