package app.revanced.integrations.swipecontrols.views

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import app.revanced.integrations.swipecontrols.SwipeControlsConfigurationProvider
import app.revanced.integrations.swipecontrols.controller.AudioVolumeController
import app.revanced.integrations.swipecontrols.controller.ScreenBrightnessController
import app.revanced.integrations.swipecontrols.controller.gesture.NoPtSSwipeGestureController
import app.revanced.integrations.swipecontrols.controller.gesture.SwipeGestureController
import app.revanced.integrations.swipecontrols.misc.Rectangle
import app.revanced.integrations.swipecontrols.misc.SwipeControlsOverlay
import app.revanced.integrations.swipecontrols.misc.SwipeZonesHelper
import app.revanced.integrations.utils.LogHelper
import app.revanced.integrations.utils.PlayerType

/**
 * The main controller for volume and brightness swipe controls
 *
 * @param hostActivity the activity that should host the controller
 * @param debugTouchableZone show a overlay on all zones covered by this layout
 */
@SuppressLint("ViewConstructor")
class SwipeControlsHostLayout(
    private val hostActivity: Activity,
    private val mainContentChild: View,
    debugTouchableZone: Boolean = false
) : FrameLayout(hostActivity) {
    init {
        isFocusable = false
        isClickable = false

        if (debugTouchableZone) {
            val zoneOverlay = View(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.argb(50, 0, 255, 0))
                z = 9999f
            }
            addView(zoneOverlay)
        }
    }

    /**
     * current instance of [AudioVolumeController]
     */
    val audio: AudioVolumeController?

    /**
     * current instance of [ScreenBrightnessController]
     */
    val screen: ScreenBrightnessController?

    /**
     * current instance of [SwipeControlsConfigurationProvider]
     */
    val config: SwipeControlsConfigurationProvider

    /**
     * current instance of [SwipeControlsOverlayLayout]
     */
    val overlay: SwipeControlsOverlay

    /**
     * main gesture controller
     */
    private val gesture: SwipeGestureController

    init {
        // create controllers
        LogHelper.info(this.javaClass, "initializing swipe controls controllers")
        config = SwipeControlsConfigurationProvider(hostActivity)
        gesture = createGestureController()
        audio = createAudioController()
        screen = createScreenController()

        // create overlay
        SwipeControlsOverlayLayout(hostActivity).let {
            overlay = it
            addView(it)
        }

        // listen for changes in the player type
        PlayerType.onChange += this::onPlayerTypeChanged
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (ev != null && gesture.onTouchEvent(ev)) true else {
            super.dispatchTouchEvent(ev)
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        // main content is always at index 0, all other are inserted after
        if (child == mainContentChild) {
            super.addView(child, 0, params)
        } else {
            super.addView(child, childCount, params)
        }
    }

    /**
     * called when the player type changes
     *
     * @param type the new player type
     */
    private fun onPlayerTypeChanged(type: PlayerType) {
        when (type) {
            PlayerType.WATCH_WHILE_FULLSCREEN -> screen?.restore()
            else -> {
                screen?.save()
                screen?.restoreDefaultBrightness()
            }
        }
    }

    /**
     * dispatch a touch event to downstream views
     *
     * @param event the event to dispatch
     * @return was the event consumed?
     */
    fun dispatchDownstreamTouchEvent(event: MotionEvent) =
        super.dispatchTouchEvent(event)

    /**
     * create the audio volume controller
     */
    private fun createAudioController() =
        if (config.enableVolumeControls)
            AudioVolumeController(context) else null

    /**
     * create the screen brightness controller instance
     */
    private fun createScreenController() =
        if (config.enableBrightnessControl)
            ScreenBrightnessController(hostActivity) else null

    /**
     * create the gesture controller based on settings
     */
    private fun createGestureController() =
        if (config.shouldEnablePressToSwipe)
            SwipeGestureController(hostActivity, this)
        else NoPtSSwipeGestureController(hostActivity, this)

    /**
     * the current screen rectangle
     */
    private val screenRect: Rectangle
        get() = Rectangle(x.toInt(), y.toInt(), width, height)

    /**
     * the rectangle of the volume control zone
     */
    val volumeZone: Rectangle
        get() = SwipeZonesHelper.getVolumeControlZone(hostActivity, screenRect)

    /**
     * the rectangle of the screen brightness control zone
     */
    val brightnessZone: Rectangle
        get() = SwipeZonesHelper.getBrightnessControlZone(hostActivity, screenRect)


    interface TouchEventListener {
        /**
         * touch event callback
         *
         * @param motionEvent the motion event that was received
         * @return intercept the event? if true, child views will not receive the event
         */
        fun onTouchEvent(motionEvent: MotionEvent): Boolean
    }

    companion object {
        /**
         * attach a [SwipeControlsHostLayout] to the activity
         *
         * @param debugTouchableZone show a overlay on all zones covered by this layout
         * @return the attached instance
         */
        @JvmStatic
        fun Activity.attachTo(debugTouchableZone: Boolean = false): SwipeControlsHostLayout {
            // get targets
            val contentView: ViewGroup = window.decorView.findViewById(android.R.id.content)!!
            var content = contentView.getChildAt(0)

            // detach previously attached swipe host first
            if (content is SwipeControlsHostLayout) {
                contentView.removeView(content)
                content.removeAllViews()
                content = content.mainContentChild
            }

            // create swipe host
            val swipeHost = SwipeControlsHostLayout(this, content, debugTouchableZone).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // insert the swipe host as parent to the actual content
            contentView.removeView(content)
            contentView.addView(swipeHost)
            swipeHost.addView(content)
            return swipeHost
        }
    }
}
