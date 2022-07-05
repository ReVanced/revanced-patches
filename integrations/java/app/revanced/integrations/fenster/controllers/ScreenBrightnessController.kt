package app.revanced.integrations.fenster.controllers

import android.app.Activity
import app.revanced.integrations.fenster.util.clamp

/**
 * controller to adjust the screen brightness level
 *
 * @param host the host activity of which the brightness is adjusted
 */
class ScreenBrightnessController(
    private val host: Activity
) {

    /**
     * the current screen brightness in percent, ranging from 0.0 to 100.0
     */
    var screenBrightness: Double
        get() {
            return host.window.attributes.screenBrightness * 100.0
        }
        set(value) {
            val attr = host.window.attributes
            attr.screenBrightness = (value.toFloat() / 100f).clamp(0f, 1f)
            host.window.attributes = attr
        }
}