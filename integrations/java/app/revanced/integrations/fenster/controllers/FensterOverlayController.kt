package app.revanced.integrations.fenster.controllers

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import app.revanced.integrations.fenster.util.applyDimension
import kotlin.math.round

/**
 * controller for the fenster overlay
 *
 * @param context the context to create the overlay in
 */
class FensterOverlayController(
    context: Context
) {

    /**
     * the main overlay view
     */
    val overlayRootView: RelativeLayout
    private val feedbackTextView: TextView

    init {
        // create root container
        overlayRootView = RelativeLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = false
            isFocusable = false
            z = 1000f
            //elevation = 1000f
        }

        // add other views
        val feedbackTextViewPadding = 2.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        feedbackTextView = TextView(context).apply {
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
                setPadding(
                    feedbackTextViewPadding,
                    feedbackTextViewPadding,
                    feedbackTextViewPadding,
                    feedbackTextViewPadding
                )
            }
            setBackgroundColor(Color.BLACK)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            visibility = View.GONE
        }
        overlayRootView.addView(feedbackTextView)
    }

    private val feedbackHideHandler = Handler(Looper.getMainLooper())
    private val feedbackHideCallback = Runnable {
        feedbackTextView.visibility = View.GONE
    }

    /**
     * set the overlay visibility
     *
     * @param visible should the overlay be visible?
     */
    fun setOverlayVisible(visible: Boolean) {
        overlayRootView.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * show the new volume level on the overlay
     *
     * @param volume the new volume level, in percent (range 0.0 - 100.0)
     */
    fun showNewVolume(volume: Double) {
        feedbackTextView.text = "Volume ${round(volume).toInt()}%"
        showFeedbackView()
    }

    /**
     * show the new screen brightness on the overlay
     *
     * @param brightness the new screen brightness, in percent (range 0.0 - 100.0)
     */
    fun showNewBrightness(brightness: Double) {
        feedbackTextView.text = "Brightness ${round(brightness).toInt()}%"
        showFeedbackView()
    }

    /**
     * notify the user that a new swipe- session has started
     */
    fun notifyEnterSwipeSession() {
        overlayRootView.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    /**
     * notify the user that fling-to-mute was triggered
     */
    fun notifyFlingToMutePerformed() {
        overlayRootView.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    /**
     * show the feedback view for a given time
     */
    private fun showFeedbackView() {
        feedbackTextView.visibility = View.VISIBLE
        feedbackHideHandler.removeCallbacks(feedbackHideCallback)
        feedbackHideHandler.postDelayed(feedbackHideCallback, 500)
    }
}
