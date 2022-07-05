package app.revanced.integrations.fenster.util

import android.content.Context
import android.util.TypedValue
import android.view.MotionEvent

/**
 * zones for swipe controls
 */
enum class SwipeControlZone {
    /**
     * not in any zone, should do nothing
     */
    NONE,

    /**
     * in volume zone, adjust volume
     */
    VOLUME_CONTROL,

    /**
     * in brightness zone, adjust brightness
     */
    BRIGHTNESS_CONTROL;
}

/**
 * get the control zone in which this motion event is
 *
 * @return the swipe control zone
 */
@Suppress("UnnecessaryVariable", "LocalVariableName")
fun MotionEvent.getSwipeControlZone(context: Context): SwipeControlZone {
    // get screen size
    val screenWidth = device.getMotionRange(MotionEvent.AXIS_X).range
    val screenHeight = device.getMotionRange(MotionEvent.AXIS_Y).range

    // check in what detection zone the event is in
    val _40dp = 40.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP).toFloat()
    val _80dp = 80.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP).toFloat()
    val _220dp = 220.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP).toFloat()

    // Y- Axis:
    // -------- 0
    //        ^
    // dead   | 40dp
    //        v
    // -------- yDeadTop
    //        ^
    // swipe  |
    //        v
    // -------- yDeadBtm
    //        ^
    // dead   | 80dp
    //        v
    // -------- screenHeight
    val yDeadTop = _40dp
    val yDeadBtm = screenHeight - _80dp

    // X- Axis:
    //  0    xBrigStart    xBrigEnd    xVolStart     xVolEnd   screenWidth
    //  |          |            |          |            |          |
    //  |   40dp   |   220dp    |          |   220dp    |   40dp   |
    //  | <------> |  <------>  | <------> |  <------>  | <------> |
    //  |   dead   | brightness |   dead   |   volume   |   dead   |
    val xBrightStart = _40dp
    val xBrightEnd = xBrightStart + _220dp
    val xVolEnd = screenWidth - _40dp
    val xVolStart = xVolEnd - _220dp

    // test detection zone
    if (y in yDeadTop..yDeadBtm) {
        return when (x) {
            in xBrightStart..xBrightEnd -> SwipeControlZone.BRIGHTNESS_CONTROL
            in xVolStart..xVolEnd -> SwipeControlZone.VOLUME_CONTROL
            else -> SwipeControlZone.NONE
        }
    }

    // not in bounds
    return SwipeControlZone.NONE
}
