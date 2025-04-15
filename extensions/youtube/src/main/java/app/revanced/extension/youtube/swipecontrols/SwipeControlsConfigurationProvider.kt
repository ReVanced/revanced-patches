package app.revanced.extension.youtube.swipecontrols

import android.graphics.Color
import app.revanced.extension.shared.StringRef.str
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.settings.Settings
import app.revanced.extension.youtube.shared.PlayerType

/**
 * provider for configuration for volume and brightness swipe controls
 */
class SwipeControlsConfigurationProvider {
//region swipe enable
    /**
     * should swipe controls be enabled? (global setting)
     */
    val enableSwipeControls: Boolean
        get() = (enableVolumeControls || enableBrightnessControl) && isFullscreenVideo

    /**
     * should swipe controls for volume be enabled?
     */
    val enableVolumeControls = Settings.SWIPE_VOLUME.get()

    /**
     * should swipe controls for volume be enabled?
     */
    val enableBrightnessControl = Settings.SWIPE_BRIGHTNESS.get()

    /**
     * is the video player currently in fullscreen mode?
     */
    private val isFullscreenVideo: Boolean
        get() = PlayerType.current == PlayerType.WATCH_WHILE_FULLSCREEN
//endregion

//region keys enable
    /**
     * should volume key controls be overwritten? (global setting)
     */
    val overwriteVolumeKeyControls: Boolean
        get() = enableVolumeControls && isFullscreenVideo
//endregion

//region gesture adjustments
    /**
     * should press-to-swipe be enabled?
     */
    val shouldEnablePressToSwipe: Boolean
        get() = Settings.SWIPE_PRESS_TO_ENGAGE.get()

    /**
     * threshold for swipe detection
     * this may be called rapidly in onScroll, so we have to load it once and then leave it constant
     */
    val swipeMagnitudeThreshold: Int
        get() = Settings.SWIPE_MAGNITUDE_THRESHOLD.get()

    /**
     * How much volume will change by single swipe.
     * If it is set to 0, it will reset to the default value because 0 would disable swiping.
     * */
    val volumeSwipeSensitivity: Int
        get() {
            val sensitivity = Settings.SWIPE_VOLUME_SENSITIVITY.get()

            if (sensitivity < 1) {
                Settings.SWIPE_VOLUME_SENSITIVITY.resetToDefault()

                return Settings.SWIPE_VOLUME_SENSITIVITY.get()
            }

            return sensitivity
        }
//endregion

//region overlay adjustments
    /**
     * should the overlay enable haptic feedback?
     */
    val shouldEnableHapticFeedback: Boolean
        get() = Settings.SWIPE_HAPTIC_FEEDBACK.get()

    /**
     * how long the overlay should be shown on changes
     */
    val overlayShowTimeoutMillis: Long
        get() = Settings.SWIPE_OVERLAY_TIMEOUT.get()

    /**
     * Gets the opacity value (0-100%) is converted to an alpha value (0-255) for transparency.
     * If the opacity value is out of range, it resets to the default and displays a warning message.
     */
    val overlayBackgroundOpacity: Int
        get() {
            var opacity = Settings.SWIPE_OVERLAY_OPACITY.get()

            if (opacity < 0 || opacity > 100) {
                Utils.showToastLong(str("revanced_swipe_overlay_background_opacity_invalid_toast"))
                Settings.SWIPE_OVERLAY_OPACITY.resetToDefault()
                opacity = Settings.SWIPE_OVERLAY_OPACITY.get()
            }

            opacity = opacity * 255 / 100
            return Color.argb(opacity, 0, 0, 0)
        }

    /**
     * The color of the progress overlay.
     */
    val overlayProgressColor: Int
        get() = 0xBFFFFFFF.toInt()

    /**
     * The color used for the background of the progress overlay fill.
     */
    val overlayFillBackgroundPaint: Int
        get() = 0x80D3D3D3.toInt()

    /**
     * The color used for the text and icons in the overlay.
     */
    val overlayTextColor: Int
        get() = Color.WHITE

    /**
     * A flag that determines if the overlay should only show the icon.
     */
    val overlayShowOverlayMinimalStyle: Boolean
        get() = Settings.SWIPE_OVERLAY_MINIMAL_STYLE.get()

    /**
     * A flag that determines if the progress bar should be circular.
     */
    val isCircularProgressBar: Boolean
        get() = Settings.SWIPE_SHOW_CIRCULAR_OVERLAY.get()

    /**
     * A flag that determines if the progress bar should be textual.
     */
    val isTextProgressBar: Boolean
        get() = Settings.SWIPE_SHOW_TEXTUAL_OVERLAY.get()
//endregion

//region behaviour

    /**
     * should the brightness be saved and restored when exiting or entering fullscreen
     */
    val shouldSaveAndRestoreBrightness: Boolean
        get() = Settings.SWIPE_SAVE_AND_RESTORE_BRIGHTNESS.get()

    /**
     * should auto-brightness be enabled at the lowest value of the brightness gesture
     */
    val shouldLowestValueEnableAutoBrightness: Boolean
        get() = Settings.SWIPE_LOWEST_VALUE_ENABLE_AUTO_BRIGHTNESS.get()

    /**
     * variable that stores the brightness gesture value in the settings
     */
    var savedScreenBrightnessValue: Float
        get() = Settings.SWIPE_BRIGHTNESS_VALUE.get()
        set(value) = Settings.SWIPE_BRIGHTNESS_VALUE.save(value)
//endregion
}
