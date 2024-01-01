package app.revanced.integrations.youtube.swipecontrols.controller

import android.view.KeyEvent
import app.revanced.integrations.youtube.swipecontrols.SwipeControlsHostActivity

/**
 * controller for custom volume button behaviour
 *
 * @param controller main controller instance
 */
class VolumeKeysController(
    private val controller: SwipeControlsHostActivity
) {
    /**
     * key event handler
     *
     * @param event the key event
     * @return consume the event?
     */
    fun onKeyEvent(event: KeyEvent): Boolean {
        if(!controller.config.overwriteVolumeKeyControls) {
            return false
        }

        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN ->
                handleVolumeKeyEvent(event, false)
            KeyEvent.KEYCODE_VOLUME_UP ->
                handleVolumeKeyEvent(event, true)
            else -> false
        }
    }

    /**
     * handle a volume up / down key event
     *
     * @param event the key event
     * @param volumeUp was the key pressed the volume up key?
     * @return consume the event?
     */
    private fun handleVolumeKeyEvent(event: KeyEvent, volumeUp: Boolean): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            controller.audio?.apply {
                volume += if (volumeUp) 1 else -1
                controller.overlay.onVolumeChanged(volume, maxVolume)
            }
        }

        return true
    }
}
