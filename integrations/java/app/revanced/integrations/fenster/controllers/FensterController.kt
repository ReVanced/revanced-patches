package app.revanced.integrations.fenster.controllers

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import app.revanced.integrations.fenster.FensterEnablement
import app.revanced.integrations.fenster.util.ScrollDistanceHelper
import app.revanced.integrations.fenster.util.SwipeControlZone
import app.revanced.integrations.fenster.util.applyDimension
import app.revanced.integrations.fenster.util.getSwipeControlZone
import app.revanced.integrations.utils.LogHelper
import kotlin.math.abs

/**
 * main controller class for 'FensterV2' swipe controls
 */
class FensterController {

    /**
     * are the swipe controls currently enabled?
     */
    var isEnabled: Boolean
        get() = _isEnabled
        set(value) {
            _isEnabled = value && FensterEnablement.shouldEnableFenster
            overlayController?.setOverlayVisible(_isEnabled)
            LogHelper.debug(this.javaClass, "FensterController.isEnabled set to $_isEnabled")
        }
    private var _isEnabled = false

    /**
     * the activity that hosts the controller
     */
    private var hostActivity: Activity? = null
    private var audioController: AudioVolumeController? = null
    private var screenController: ScreenBrightnessController? = null
    private var overlayController: FensterOverlayController? = null

    private var gestureListener: FensterGestureListener? = null
    private var gestureDetector: GestureDetector? = null

    /**
     * Initializes the controller.
     * this function *may* be called after [initializeOverlay], but must be called before [onTouchEvent]
     *
     * @param host the activity that hosts the controller. this must be the same activity that the view hook for [onTouchEvent] is on
     */
    fun initializeController(host: Activity) {
        if (hostActivity != null) {
            if (host == hostActivity) {
                // function was called twice, ignore the call
                LogHelper.debug(
                    this.javaClass,
                    "initializeController was called twice, ignoring secondary call"
                )
                return
            }
        }

        LogHelper.debug(this.javaClass, "initializing FensterV2 controllers")
        hostActivity = host
        audioController = if (FensterEnablement.shouldEnableFensterVolumeControl)
            AudioVolumeController(host) else null
        screenController = if (FensterEnablement.shouldEnableFensterBrightnessControl)
            ScreenBrightnessController(host) else null

        gestureListener = FensterGestureListener(host)
        gestureDetector = GestureDetector(host, gestureListener)
    }

    /**
     * Initializes the user feedback overlay, adding it as a child to the provided parent.
     * this function *may* not be called, but in that case you'll have no user feedback
     *
     * @param parent parent view group that the overlay is added to
     */
    fun initializeOverlay(parent: ViewGroup) {
        LogHelper.debug(this.javaClass, "initializing FensterV2 overlay")

        // create and add overlay
        overlayController = FensterOverlayController(parent.context)
        parent.addView(overlayController!!.overlayRootView, 0)
    }

    /**
     * Process touch events from the view hook.
     * the hooked view *must* be a child of the activity used for [initializeController]
     *
     * @param event the motion event to process
     * @return was the event consumed by the controller?
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        // if disabled, we shall not consume any events
        if (!isEnabled) return false

        // if important components are not present, there is no point in processing the event here
        if (hostActivity == null || gestureDetector == null || gestureListener == null) {
            return false
        }

        // send event to gesture detector
        if (event.action == MotionEvent.ACTION_UP) {
            gestureListener?.onUp(event)
        }
        val consumed = gestureDetector?.onTouchEvent(event) ?: false

        // if the event was inside a control zone, we always consume the event
        val swipeZone = event.getSwipeControlZone(hostActivity!!)
        var inControlZone = false
        if (audioController != null) {
            inControlZone = inControlZone || swipeZone == SwipeControlZone.VOLUME_CONTROL
        }
        if (screenController != null) {
            inControlZone = inControlZone || swipeZone == SwipeControlZone.BRIGHTNESS_CONTROL
        }

        return consumed || inControlZone
    }

    /**
     * primary gesture listener that handles the following behaviour:
     *
     * - Volume & Brightness swipe controls:
     * when swiping on the right or left side of the screen, the volume or brightness is adjusted accordingly.
     * swipe controls are only unlocked after a long- press in the corresponding screen half
     *
     * - Fling- to- mute:
     * when quickly flinging down, the volume is instantly muted
     */
    inner class FensterGestureListener(
        private val context: Context
    ) : GestureDetector.SimpleOnGestureListener() {

        /**
         * to enable swipe controls, users must first long- press. this flags monitors that long- press
         */
        private var inSwipeSession = false

        /**
         * scroller for volume adjustment
         */
        private val volumeScroller = ScrollDistanceHelper(
            10.applyDimension(
                context,
                TypedValue.COMPLEX_UNIT_DIP
            )
        ) { _, _, direction ->
            audioController?.apply {
                volume += direction
                overlayController?.showNewVolume((volume * 100.0) / maxVolume)
            }
        }

        /**
         * scroller for screen brightness adjustment
         */
        private val brightnessScroller = ScrollDistanceHelper(
            1.applyDimension(
                context,
                TypedValue.COMPLEX_UNIT_DIP
            )
        ) { _, _, direction ->
            screenController?.apply {
                screenBrightness += direction
                overlayController?.showNewBrightness(screenBrightness)
            }
        }

        /**
         * custom handler for ACTION_UP event, because GestureDetector doesn't offer that :|
         *
         * @param e the motion event
         */
        fun onUp(e: MotionEvent) {
            LogHelper.debug(this.javaClass, "onUp(${e.x}, ${e.y}, ${e.action})")
            inSwipeSession = false
            volumeScroller.reset()
            brightnessScroller.reset()
        }

        override fun onLongPress(e: MotionEvent?) {
            if (e == null) return
            LogHelper.debug(this.javaClass, "onLongPress(${e.x}, ${e.y}, ${e.action})")

            // enter swipe session with feedback
            inSwipeSession = true
            overlayController?.notifyEnterSwipeSession()

            // make the GestureDetector believe there was a ACTION_UP event
            // so it will handle further events
            e.action = MotionEvent.ACTION_UP
            gestureDetector?.onTouchEvent(e)
        }

        override fun onScroll(
            eFrom: MotionEvent?,
            eTo: MotionEvent?,
            disX: Float,
            disY: Float
        ): Boolean {
            if (eFrom == null || eTo == null) return false
            LogHelper.debug(
                this.javaClass,
                "onScroll(from: [${eFrom.x}, ${eFrom.y}, ${eFrom.action}], to: [${eTo.x}, ${eTo.y}, ${eTo.action}], d: [$disX, $disY])"
            )

            // ignore if scroll not in scroll session
            if (!inSwipeSession) return false

            // do the adjustment
            when (eFrom.getSwipeControlZone(context)) {
                SwipeControlZone.VOLUME_CONTROL -> {
                    volumeScroller.add(disY.toDouble())
                }
                SwipeControlZone.BRIGHTNESS_CONTROL -> {
                    brightnessScroller.add(disY.toDouble())
                }
                SwipeControlZone.NONE -> {}
            }
            return true
        }

        override fun onFling(
            eFrom: MotionEvent?,
            eTo: MotionEvent?,
            velX: Float,
            velY: Float
        ): Boolean {
            if (eFrom == null || eTo == null) return false
            LogHelper.debug(
                this.javaClass,
                "onFling(from: [${eFrom.x}, ${eFrom.y}, ${eFrom.action}], to: [${eTo.x}, ${eTo.y}, ${eTo.action}], v: [$velX, $velY])"
            )

            // filter out flings that are not very vertical
            if (abs(velY) < abs(velX * 2)) return false

            // check if either of the events was in the volume zone
            if ((eFrom.getSwipeControlZone(context) == SwipeControlZone.VOLUME_CONTROL)
                || (eTo.getSwipeControlZone(context) == SwipeControlZone.VOLUME_CONTROL)
            ) {
                // if the fling was very aggressive, trigger instant- mute
                if (velY > 5000) {
                    audioController?.apply {
                        volume = 0
                        overlayController?.notifyFlingToMutePerformed()
                        overlayController?.showNewVolume((volume * 100.0) / maxVolume)
                    }
                }
            }

            return true
        }
    }
}
