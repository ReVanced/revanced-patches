package app.revanced.extension.youtube.swipecontrols.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import app.revanced.extension.shared.StringRef.str
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.swipecontrols.SwipeControlsConfigurationProvider
import app.revanced.extension.youtube.swipecontrols.misc.SwipeControlsOverlay
import app.revanced.extension.youtube.swipecontrols.misc.applyDimension
import kotlin.math.round

/**
 * main overlay layout for volume and brightness swipe controls
 *
 * @param context context to create in
 */
class SwipeControlsOverlayLayout(
    context: Context,
    private val config: SwipeControlsConfigurationProvider,
) : RelativeLayout(context), SwipeControlsOverlay {
    /**
     * DO NOT use this, for tools only
     */
    constructor(context: Context) : this(context, SwipeControlsConfigurationProvider(context))

    private val feedbackTextView: TextView
    private val autoBrightnessIcon: Drawable
    private val manualBrightnessIcon: Drawable
    private val mutedVolumeIcon: Drawable
    private val normalVolumeIcon: Drawable

    private fun getDrawable(name: String, width: Int, height: Int): Drawable {
        return resources.getDrawable(
            Utils.getResourceIdentifier(context, name, "drawable"),
            context.theme,
        ).apply {
            setTint(config.overlayForegroundColor)
            setBounds(
                0,
                0,
                width,
                height,
            )
        }
    }

    init {
        // init views
        val feedbackTextViewPadding = 2.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        val compoundIconPadding = 4.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        feedbackTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
            ).apply {
                addRule(CENTER_IN_PARENT, TRUE)
                setPadding(
                    feedbackTextViewPadding,
                    feedbackTextViewPadding,
                    feedbackTextViewPadding,
                    feedbackTextViewPadding,
                )
            }
            background = GradientDrawable().apply {
                cornerRadius = 8f
                setColor(config.overlayTextBackgroundColor)
            }
            setTextColor(config.overlayForegroundColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, config.overlayTextSize.toFloat())
            compoundDrawablePadding = compoundIconPadding
            visibility = GONE
        }
        addView(feedbackTextView)

        // get icons scaled, assuming square icons
        val iconHeight = round(feedbackTextView.lineHeight * .8).toInt()
        autoBrightnessIcon = getDrawable("revanced_ic_sc_brightness_auto", iconHeight, iconHeight)
        manualBrightnessIcon = getDrawable("revanced_ic_sc_brightness_manual", iconHeight, iconHeight)
        mutedVolumeIcon = getDrawable("revanced_ic_sc_volume_mute", iconHeight, iconHeight)
        normalVolumeIcon = getDrawable("revanced_ic_sc_volume_normal", iconHeight, iconHeight)
    }

    private val feedbackHideHandler = Handler(Looper.getMainLooper())
    private val feedbackHideCallback = Runnable {
        feedbackTextView.visibility = GONE
    }

    /**
     * show the feedback view for a given time
     *
     * @param message the message to show
     * @param icon the icon to use
     */
    private fun showFeedbackView(message: String, icon: Drawable) {
        feedbackHideHandler.removeCallbacks(feedbackHideCallback)
        feedbackHideHandler.postDelayed(feedbackHideCallback, config.overlayShowTimeoutMillis)
        feedbackTextView.apply {
            text = message
            setCompoundDrawablesRelative(
                icon,
                null,
                null,
                null,
            )
            visibility = VISIBLE
        }
    }

    override fun onVolumeChanged(newVolume: Int, maximumVolume: Int) {
        showFeedbackView(
            "$newVolume",
            if (newVolume > 0) normalVolumeIcon else mutedVolumeIcon,
        )
    }

    override fun onBrightnessChanged(brightness: Double) {
        if (config.shouldLowestValueEnableAutoBrightness && brightness <= 0) {
            showFeedbackView(
                str("revanced_swipe_lowest_value_enable_auto_brightness_overlay_text"),
                autoBrightnessIcon,
            )
        } else if (brightness >= 0) {
            showFeedbackView("${round(brightness).toInt()}%", manualBrightnessIcon)
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
