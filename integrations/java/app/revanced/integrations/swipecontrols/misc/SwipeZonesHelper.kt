package app.revanced.integrations.swipecontrols.misc

import android.content.Context
import android.util.TypedValue

//TODO reimplement this, again with 1/3rd for the zone size
// because in shorts, the screen is way less wide than this code expects!
/**
 * Y- Axis:
 * -------- 0
 *        ^
 * dead   | 40dp
 *        v
 * -------- yDeadTop
 *        ^
 * swipe  |
 *        v
 * -------- yDeadBtm
 *        ^
 * dead   | 80dp
 *        v
 * -------- screenHeight
 *
 * X- Axis:
 *  0    xBrigStart    xBrigEnd    xVolStart     xVolEnd   screenWidth
 *  |          |            |          |            |          |
 *  |   40dp   |   200dp    |          |   200dp    |   40dp   |
 *  | <------> |  <------>  | <------> |  <------>  | <------> |
 *  |   dead   | brightness |   dead   |   volume   |   dead   |
 */
@Suppress("LocalVariableName")
object SwipeZonesHelper {

    /**
     * get the zone for volume control
     *
     * @param context the current context
     * @param screenRect the screen rectangle in the current orientation
     * @return the rectangle for the control zone
     */
    fun getVolumeControlZone(context: Context, screenRect: Rectangle): Rectangle {
        val _40dp = 40.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        val _80dp = 80.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        val _200dp = 200.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)

        return Rectangle(
            screenRect.right - _40dp - _200dp,
            screenRect.top + _40dp,
            _200dp,
            screenRect.height - _40dp - _80dp
        )
    }

    /**
     * get the zone for brightness control
     *
     * @param context the current context
     * @param screenRect the screen rectangle in the current orientation
     * @return the rectangle for the control zone
     */
    fun getBrightnessControlZone(context: Context, screenRect: Rectangle): Rectangle {
        val _40dp = 40.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        val _80dp = 80.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)
        val _200dp = 200.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)

        return Rectangle(
            screenRect.left + _40dp,
            screenRect.top + _40dp,
            _200dp,
            screenRect.height - _40dp - _80dp
        )
    }
}
