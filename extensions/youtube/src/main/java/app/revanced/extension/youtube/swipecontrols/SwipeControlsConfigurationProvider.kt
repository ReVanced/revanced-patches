package app.revanced.extension.youtube.swipecontrols

import android.content.Context
import android.graphics.Color
import app.revanced.extension.shared.StringRef.str
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.settings.Settings
import app.revanced.extension.youtube.shared.PlayerType

/**
 * provider for configuration for volume and brightness swipe controls
 *
 * @param context the context to create in
 */
class SwipeControlsConfigurationProvider(
    private val context: Context,
) {
//region swipe enable
    /**
     * should swipe controls be enabled? (global setting)
     */
    val enableSwipeControls: Boolean
        get() = isFullscreenVideo && (enableVolumeControls || enableBrightnessControl)

    /**
     * should swipe controls for volume be enabled?
     */
    val enableVolumeControls: Boolean
        get() = Settings.SWIPE_VOLUME.get()

    /**
     * should swipe controls for volume be enabled?
     */
    val enableBrightnessControl: Boolean
        get() = Settings.SWIPE_BRIGHTNESS.get()

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
        get() = isFullscreenVideo && enableVolumeControls
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
     * text size for the overlay, in sp
     */
    val overlayTextSize: Int
        get() {
            var textSize = Settings.SWIPE_OVERLAY_TEXT_SIZE.get()

            if (textSize <= 0 || textSize > 30) {
                Utils.showToastLong(str("revanced_swipe_overlay_text_size_invalid_toast"))
                Settings.SWIPE_OVERLAY_TEXT_SIZE.resetToDefault()
                textSize = Settings.SWIPE_OVERLAY_TEXT_SIZE.get()
            }

            return textSize
        }

    /**
     * get the background color for text on the overlay, as a color int
     */
    val overlayTextBackgroundColor: Int
        get() {
            var opacity = Settings.SWIPE_OVERLAY_OPACITY.get()

            if (opacity < 0 || opacity > 100) {
                Utils.showToastLong(str("revanced_swipe_text_overlay_size_invalid_toast"))
                Settings.SWIPE_OVERLAY_OPACITY.resetToDefault()
                opacity = Settings.SWIPE_OVERLAY_OPACITY.get()
            }

            opacity = opacity * 255 / 100
            return Color.argb(opacity, 0, 0, 0)
        }

    /**
     * get the foreground color for text on the overlay, as a color int
     */
    val overlayForegroundColor: Int
        get() = Color.WHITE

    /**
     * If "show only icon" is selected, the background and text will be hidden,
     * and only the icon will be displayed.
     */
    val overlayTextBackgroundOnlyIcon: Boolean
        get() = Settings.SWIPE_SHOW_ONLY_ICON.get()

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
