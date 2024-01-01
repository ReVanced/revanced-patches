package app.revanced.integrations.youtube.swipecontrols.controller.gesture

import android.view.MotionEvent
import app.revanced.integrations.youtube.shared.PlayerControlsVisibilityObserver
import app.revanced.integrations.youtube.shared.PlayerControlsVisibilityObserverImpl
import app.revanced.integrations.youtube.swipecontrols.SwipeControlsHostActivity
import app.revanced.integrations.youtube.swipecontrols.controller.gesture.core.BaseGestureController
import app.revanced.integrations.youtube.swipecontrols.controller.gesture.core.SwipeDetector
import app.revanced.integrations.youtube.swipecontrols.misc.contains
import app.revanced.integrations.youtube.swipecontrols.misc.toPoint

/**
 * provides the classic swipe controls experience, as it was with 'XFenster'
 *
 * @param controller reference to the main swipe controller
 */
class ClassicSwipeController(
    private val controller: SwipeControlsHostActivity
) : BaseGestureController(controller),
    PlayerControlsVisibilityObserver by PlayerControlsVisibilityObserverImpl(controller) {
    /**
     * the last event captured in [onDown]
     */
    private var lastOnDownEvent: MotionEvent? = null

    override val shouldForceInterceptEvents: Boolean
        get() = currentSwipe == SwipeDetector.SwipeDirection.VERTICAL

    override fun isInSwipeZone(motionEvent: MotionEvent): Boolean {
        val inVolumeZone = if (controller.config.enableVolumeControls)
            (motionEvent.toPoint() in controller.zones.volume) else false
        val inBrightnessZone = if (controller.config.enableBrightnessControl)
            (motionEvent.toPoint() in controller.zones.brightness) else false

        return inVolumeZone || inBrightnessZone
    }

    override fun shouldDropMotion(motionEvent: MotionEvent): Boolean {
        // ignore gestures with more than one pointer
        // when such a gesture is detected, dispatch the first event of the gesture to downstream
        if (motionEvent.pointerCount > 1) {
            lastOnDownEvent?.let {
                controller.dispatchDownstreamTouchEvent(it)
                it.recycle()
            }
            lastOnDownEvent = null
            return true
        }

        // ignore gestures when player controls are visible
        return arePlayerControlsVisible
    }

    override fun onDown(motionEvent: MotionEvent): Boolean {
        // save the event for later
        lastOnDownEvent?.recycle()
        lastOnDownEvent = MotionEvent.obtain(motionEvent)

        // must be inside swipe zone
        return isInSwipeZone(motionEvent)
    }

    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
        MotionEvent.obtain(motionEvent).let {
            it.action = MotionEvent.ACTION_DOWN
            controller.dispatchDownstreamTouchEvent(it)
            it.recycle()
        }

        return false
    }

    override fun onDoubleTapEvent(motionEvent: MotionEvent): Boolean {
        MotionEvent.obtain(motionEvent).let {
            controller.dispatchDownstreamTouchEvent(it)
            it.recycle()
        }

        return super.onDoubleTapEvent(motionEvent)
    }

    override fun onLongPress(motionEvent: MotionEvent) {
        MotionEvent.obtain(motionEvent).let {
            controller.dispatchDownstreamTouchEvent(it)
            it.recycle()
        }

        super.onLongPress(motionEvent)
    }

    override fun onSwipe(
        from: MotionEvent,
        to: MotionEvent,
        distanceX: Double,
        distanceY: Double
    ): Boolean {
        // cancel if not vertical
        if (currentSwipe != SwipeDetector.SwipeDirection.VERTICAL) return false
        return when (from.toPoint()) {
            in controller.zones.volume -> {
                scrollVolume(distanceY)
                true
            }
            in controller.zones.brightness -> {
                scrollBrightness(distanceY)
                true
            }
            else -> false
        }
    }
}
