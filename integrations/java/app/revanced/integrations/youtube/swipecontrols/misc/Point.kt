package app.revanced.integrations.youtube.swipecontrols.misc

import android.view.MotionEvent

/**
 * a simple 2D point class
 */
data class Point(
    val x: Int,
    val y: Int
)

/**
 * convert the motion event coordinates to a point
 */
fun MotionEvent.toPoint(): Point =
    Point(x.toInt(), y.toInt())
