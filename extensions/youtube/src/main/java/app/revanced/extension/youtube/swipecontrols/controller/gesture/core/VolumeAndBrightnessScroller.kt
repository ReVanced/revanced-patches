package app.revanced.extension.youtube.swipecontrols.controller.gesture.core

import android.content.Context
import android.util.TypedValue
import app.revanced.extension.youtube.swipecontrols.controller.AudioVolumeController
import app.revanced.extension.youtube.swipecontrols.controller.ScreenBrightnessController
import app.revanced.extension.youtube.swipecontrols.misc.ScrollDistanceHelper
import app.revanced.extension.youtube.swipecontrols.misc.SwipeControlsOverlay
import app.revanced.extension.youtube.swipecontrols.misc.applyDimension

/**
 * describes a class that controls volume and brightness based on scrolling events
 */
interface VolumeAndBrightnessScroller {
    /**
     * submit a scroll for volume adjustment
     *
     * @param distance the scroll distance
     */
    fun scrollVolume(distance: Double)

    /**
     * submit a scroll for brightness adjustment
     *
     * @param distance the scroll distance
     */
    fun scrollBrightness(distance: Double)

    /**
     * reset all scroll distances to zero
     */
    fun resetScroller()
}

/**
 * handles scrolling of volume and brightness, adjusts them using the provided controllers and updates the overlay
 *
 * @param context context to create the scrollers in
 * @param volumeController volume controller instance. if null, volume control is disabled
 * @param screenController screen brightness controller instance. if null, brightness control is disabled
 * @param overlayController overlay controller instance
 * @param volumeDistance unit distance for volume scrolling, in dp
 * @param brightnessDistance unit distance for brightness scrolling, in dp
 * @param volumeSwipeSensitivity how much volume will change by single swipe
 */
class VolumeAndBrightnessScrollerImpl(
    context: Context,
    private val volumeController: AudioVolumeController?,
    private val screenController: ScreenBrightnessController?,
    private val overlayController: SwipeControlsOverlay,
    volumeDistance: Int = 10,
    brightnessDistance: Int = 1,
    private val volumeSwipeSensitivity: Int,
) : VolumeAndBrightnessScroller {

    // region volume
    private val volumeScroller =
        ScrollDistanceHelper(
            volumeDistance.applyDimension(
                context,
                TypedValue.COMPLEX_UNIT_DIP,
            ),
        ) { _, _, direction ->
            volumeController?.run {
                volume += direction * volumeSwipeSensitivity
                overlayController.onVolumeChanged(volume, maxVolume)
            }
        }

    override fun scrollVolume(distance: Double) = volumeScroller.add(distance)
    //endregion

    //region brightness
    private val brightnessScroller =
        ScrollDistanceHelper(
            brightnessDistance.applyDimension(
                context,
                TypedValue.COMPLEX_UNIT_DIP,
            ),
        ) { _, _, direction ->
            screenController?.run {
                val shouldAdjustBrightness = if (host.config.shouldLowestValueEnableAutoBrightness) {
                    screenBrightness > 0 || direction > 0
                } else {
                    screenBrightness >= 0 || direction >= 0
                }

                if (shouldAdjustBrightness) {
                    screenBrightness += direction
                } else {
                    restoreDefaultBrightness()
                }
                overlayController.onBrightnessChanged(screenBrightness)
            }
        }

    override fun scrollBrightness(distance: Double) = brightnessScroller.add(distance)
    //endregion

    override fun resetScroller() {
        volumeScroller.reset()
        brightnessScroller.reset()
    }
}
