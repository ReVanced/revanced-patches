package app.revanced.integrations.youtube.swipecontrols.misc

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

fun Float.clamp(min: Float, max: Float): Float {
    if (this < min) return min
    if (this > max) return max
    return this
}

fun Int.clamp(min: Int, max: Int): Int {
    if (this < min) return min
    if (this > max) return max
    return this
}

fun Int.applyDimension(context: Context, unit: Int): Int {
    return TypedValue.applyDimension(
        unit,
        this.toFloat(),
        context.resources.displayMetrics
    ).roundToInt()
}

