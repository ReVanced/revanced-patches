package app.revanced.integrations.swipecontrols.controller.gesture

import android.view.MotionEvent
import app.revanced.integrations.swipecontrols.SwipeControlsHostActivity

/**
 * [SwipeGestureController], but with press-to-swipe disabled because a lot of people dislike the feature.
 * If you want to change something, try to do it in [SwipeGestureController] so that both configurations can benefit from it
 */
class NoPtSSwipeGestureController(controller: SwipeControlsHostActivity) :
    SwipeGestureController(controller) {

    /**
     * to disable press-to-swipe, we have to become press-to-swipe
     */
    override var inSwipeSession
        get() = true
        set(_) {}

    override fun onLongPress(e: MotionEvent?) {
        if (e == null) return

        // send GestureDetector a ACTION_CANCEL event so it will handle further events
        // if this is left out, swipe-to-dismiss is triggered when scrolling down
        e.action = MotionEvent.ACTION_CANCEL
        detector.onTouchEvent(e)
    }
}
