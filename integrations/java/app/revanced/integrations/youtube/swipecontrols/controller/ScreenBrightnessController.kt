package app.revanced.integrations.youtube.swipecontrols.controller

import android.app.Activity
import android.view.WindowManager
import app.revanced.integrations.youtube.swipecontrols.misc.clamp

/**
 * controller to adjust the screen brightness level
 *
 * @param host the host activity of which the brightness is adjusted
 */
class ScreenBrightnessController(
    private val host: Activity
) {
    /**
     * screen brightness saved by [save]
     */
    private var savedScreenBrightness: Float? = null

    /**
     * the current screen brightness in percent, ranging from 0.0 to 100.0
     */
    var screenBrightness: Double
        get() = rawScreenBrightness * 100.0
        set(value) {
            rawScreenBrightness = (value.toFloat() / 100f).clamp(0f, 1f)
        }

    /**
     * restore the screen brightness to the default device brightness
     */
    fun restoreDefaultBrightness() {
        rawScreenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }

    /**
     * is the screen brightness set to device- default?
     */
    val isDefaultBrightness
        get() = (rawScreenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)

    /**
     * save the current screen brightness, to be brought back using [restore]
     */
    fun save() {
        if (savedScreenBrightness == null) {
            savedScreenBrightness = rawScreenBrightness
        }
    }

    /**
     * restore the screen brightness saved using [save]
     */
    fun restore() {
        savedScreenBrightness?.let {
            rawScreenBrightness = it
        }
        savedScreenBrightness = null
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