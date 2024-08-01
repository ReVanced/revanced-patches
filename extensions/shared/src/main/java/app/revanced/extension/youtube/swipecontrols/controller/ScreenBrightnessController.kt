package app.revanced.extension.youtube.swipecontrols.controller

import android.view.WindowManager
import app.revanced.extension.youtube.swipecontrols.SwipeControlsHostActivity
import app.revanced.extension.youtube.swipecontrols.misc.clamp

/**
 * controller to adjust the screen brightness level
 *
 * @param host the host activity of which the brightness is adjusted, the main controller instance
 */
class ScreenBrightnessController(
    val host: SwipeControlsHostActivity,
) {

    /**
     * the current screen brightness in percent, ranging from 0.0 to 100.0
     */
    var screenBrightness: Double
        get() = rawScreenBrightness * 100.0
        set(value) {
            rawScreenBrightness = (value.toFloat() / 100f).clamp(0f, 1f)
        }

    /**
     * is the screen brightness set to device- default?
     */
    val isDefaultBrightness
        get() = (rawScreenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)

    /**
     * restore the screen brightness to the default device brightness
     */
    fun restoreDefaultBrightness() {
        rawScreenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }

    // Flag that indicates whether the brightness has been restored
    private var isBrightnessRestored = false

    /**
     * save the current screen brightness into settings, to be brought back using [restore]
     */
    fun save() {
        if (isBrightnessRestored) {
            // Saves the current screen brightness value into settings
            host.config.savedScreenBrightnessValue = rawScreenBrightness
            // Reset the flag
            isBrightnessRestored = false
        }
    }

    /**
     * restore the screen brightness from settings saved using [save]
     */
    fun restore() {
        // Restores the screen brightness value from the saved settings
        rawScreenBrightness = host.config.savedScreenBrightnessValue
        // Mark that brightness has been restored
        isBrightnessRestored = true
    }

    /**
     * wrapper for the raw screen brightness in [WindowManager.LayoutParams.screenBrightness]
     */
    var rawScreenBrightness: Float
        get() = host.window.attributes.screenBrightness
        private set(value) {
            val attr = host.window.attributes
            attr.screenBrightness = value
            host.window.attributes = attr
        }
}
