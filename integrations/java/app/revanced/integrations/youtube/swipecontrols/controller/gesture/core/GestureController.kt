package app.revanced.integrations.youtube.swipecontrols.controller.gesture.core

import android.view.MotionEvent

/**
 * describes a class that accepts motion events and detects gestures
 */
interface GestureController {
    /**
     * accept a touch event and try to detect the desired gestures using it
     *
     * @param motionEvent the motion event that was submitted
     * @return was a gesture detected?
     */
    fun submitTouchEvent(motionEvent: MotionEvent): Boolean
}
