package app.revanced.integrations.fenster

import app.revanced.integrations.settings.SettingsEnum

/**
 * controls fenster feature enablement
 */
object FensterEnablement {

    /**
     * should fenster be enabled? (global setting)
     */
    val shouldEnableFenster: Boolean
        get() {
            return shouldEnableFensterVolumeControl || shouldEnableFensterBrightnessControl
        }

    /**
     * should swipe controls for volume be enabled?
     */
    val shouldEnableFensterVolumeControl: Boolean
        get() {
            return SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.boolean
        }

    /**
     * should swipe controls for volume be enabled?
     */
    val shouldEnableFensterBrightnessControl: Boolean
        get() {
            return SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.boolean
        }
}