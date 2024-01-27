package app.revanced.integrations.youtube.swipecontrols.controller.gesture.core

import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.pow

/**
 * describes a class that can detect swipes and their directionality
 */
interface SwipeDetector {
    /**
     * the currently detected swipe
     */
    val currentSwipe: SwipeDirection

    /**
     * submit a onScroll event for swipe detection
     *
     * @param from start event
     * @param to end event
     * @param distanceX horizontal scroll distance
     * @param distanceY vertical scroll distance
     */
    fun submitForSwipe(
        from: MotionEvent,
        to: MotionEvent,
        distanceX: Float,
        distanceY: Float
    )

    /**
     * reset the swipe detection
     */
    fun resetSwipe()

    /**
     * direction of a swipe
     */
    enum class SwipeDirection {
        /**
         * swipe has no direction or no swipe
         */
        NONE,

        /**
         * swipe along the X- Axes
         */
        HORIZONTAL,

        /**
         * swipe along the Y- Axes
         */
        VERTICAL
    }
}

/**
 * detector that can detect swipes and their directionality
 *
 * @param swipeMagnitudeThreshold minimum magnitude before a swipe is detected as such
 */
class SwipeDetectorImpl(
    private val swipeMagnitudeThreshold: Double
) : SwipeDetector {
    override var currentSwipe = SwipeDetector.SwipeDirection.NONE

    override fun submitForSwipe(
        from: MotionEvent,
        to: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ) {
        if (currentSwipe == SwipeDetector.SwipeDirection.NONE) {
            // no swipe direction was detected yet, try to detect one
            // if the user did not swipe far enough, we cannot detect what direction they swiped
            // so we wait until a greater distance was swiped
            // NOTE: sqrt() can be high- cost, so using squared magnitudes here
            val deltaX = abs(to.x - from.x)
            val deltaY = abs(to.y - from.y)
            val swipeMagnitudeSquared = deltaX.pow(2) + deltaY.pow(2)
            if (swipeMagnitudeSquared > swipeMagnitudeThreshold.pow(2)) {
                currentSwipe = if (deltaY > deltaX) {
                    SwipeDetector.SwipeDirection.VERTICAL
                } else {
                    SwipeDetector.SwipeDirection.HORIZONTAL
                }
            }
        }
    }

    override fun resetSwipe() {
        currentSwipe = SwipeDetector.SwipeDirection.NONE
    }
}
