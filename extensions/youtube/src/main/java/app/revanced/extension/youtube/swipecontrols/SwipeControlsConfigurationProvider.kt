package app.revanced.extension.youtube.swipecontrols

import android.annotation.SuppressLint
import android.graphics.Color
import app.revanced.extension.shared.Logger
import app.revanced.extension.shared.StringRef.str
import app.revanced.extension.shared.Utils
import app.revanced.extension.youtube.settings.Settings
import app.revanced.extension.youtube.shared.PlayerType

/**
 * Provides configuration settings for volume and brightness swipe controls in the YouTube player.
 * Manages enabling/disabling gestures, overlay appearance, and behavior preferences.
 */
class SwipeControlsConfigurationProvider {
    //region swipe enable
    /**
     * Indicates whether swipe controls are enabled globally.
     * Returns true if either volume or brightness controls are enabled and the video is in fullscreen mode.
     */
    val enableSwipeControls: Boolean
        get() = (enableVolumeControls || enableBrightnessControl) && isFullscreenVideo

    /**
     * Indicates whether swipe controls for adjusting volume are enabled.
     */
    val enableVolumeControls = Settings.SWIPE_VOLUME.get()

    /**
     * Indicates whether swipe controls for adjusting brightness are enabled.
     */
    val enableBrightnessControl = Settings.SWIPE_BRIGHTNESS.get()

    /**
     * Checks if the video player is currently in fullscreen mode.
     */
    private val isFullscreenVideo: Boolean
        get() = PlayerType.current == PlayerType.WATCH_WHILE_FULLSCREEN
    //endregion

    //region keys enable
    /**
     * Indicates whether volume key controls should be overridden by swipe controls.
     * Returns true if volume controls are enabled and the video is in fullscreen mode.
     */
    val overwriteVolumeKeyControls: Boolean
        get() = enableVolumeControls && isFullscreenVideo
    //endregion

    //region gesture adjustments
    /**
     * Indicates whether press-to-swipe mode is enabled, requiring a press before swiping to activate controls.
     */
    val shouldEnablePressToSwipe: Boolean
        get() = Settings.SWIPE_PRESS_TO_ENGAGE.get()

    /**
     * The threshold for detecting swipe gestures, in pixels.
     * Loaded once to ensure consistent behavior during rapid scroll events.
     */
    val swipeMagnitudeThreshold: Int
        get() = Settings.SWIPE_MAGNITUDE_THRESHOLD.get()

    /**
     * The sensitivity of volume swipe gestures, determining how much volume changes per swipe.
     * Resets to default if set to 0, as it would disable swiping.
     */
    val volumeSwipeSensitivity: Int
        get() {
            val sensitivity = Settings.SWIPE_VOLUME_SENSITIVITY.get()

            if (sensitivity < 1) {
                return Settings.SWIPE_VOLUME_SENSITIVITY.resetToDefault()
            }

            return sensitivity
        }
    //endregion

    //region overlay adjustments
    /**
     * Indicates whether haptic feedback should be enabled for swipe control interactions.
     */
    val shouldEnableHapticFeedback: Boolean
        get() = Settings.SWIPE_HAPTIC_FEEDBACK.get()

    /**
     * The duration in milliseconds that the overlay should remain visible after a change.
     */
    val overlayShowTimeoutMillis: Long
        get() = Settings.SWIPE_OVERLAY_TIMEOUT.get()

    /**
     * The background opacity of the overlay, converted from a percentage (0-100) to an alpha value (0-255).
     * Resets to default and shows a toast if the value is out of range.
     */
    val overlayBackgroundOpacity: Int
        get() {
            var opacity = Settings.SWIPE_OVERLAY_OPACITY.get()

            if (opacity < 0 || opacity > 100) {
                Utils.showToastLong(str("revanced_swipe_overlay_background_opacity_invalid_toast"))
                opacity = Settings.SWIPE_OVERLAY_OPACITY.resetToDefault()
            }

            opacity = opacity * 255 / 100
            return Color.argb(opacity, 0, 0, 0)
        }

    /**
     * The color of the progress bar in the overlay for brightness.
     * Resets to default and shows a toast if the color string is invalid or empty.
     */
    val overlayBrightnessProgressColor: Int
        get() {
            try {
                @SuppressLint("UseKtx")
                val color = Color.parseColor(Settings.SWIPE_OVERLAY_BRIGHTNESS_COLOR.get())
                return (0xBF000000.toInt() or (color and 0xFFFFFF))
            } catch (ex: IllegalArgumentException) {
                Logger.printDebug({ "Could not parse brightness color" }, ex)
                Utils.showToastLong(str("revanced_swipe_overlay_progress_color_invalid_toast"))
                Settings.SWIPE_OVERLAY_BRIGHTNESS_COLOR.resetToDefault()
                return overlayBrightnessProgressColor // Recursively return.
            }
        }

    /**
     * The color of the progress bar in the overlay for volume.
     * Resets to default and shows a toast if the color string is invalid or empty.
     */
    val overlayVolumeProgressColor: Int
        get() {
            try {
                @SuppressLint("UseKtx")
                val color = Color.parseColor(Settings.SWIPE_OVERLAY_VOLUME_COLOR.get())
                return (0xBF000000.toInt() or (color and 0xFFFFFF))
            } catch (ex: IllegalArgumentException) {
                Logger.printDebug({ "Could not parse volume color" }, ex)
                Utils.showToastLong(str("revanced_swipe_overlay_progress_color_invalid_toast"))
                Settings.SWIPE_OVERLAY_VOLUME_COLOR.resetToDefault()
                return overlayVolumeProgressColor // Recursively return.
            }
        }

    /**
     * The background color used for the filled portion of the progress bar in the overlay.
     */
    val overlayFillBackgroundPaint: Int
        get() = 0x80D3D3D3.toInt()

    /**
     * The color used for text and icons in the overlay.
     */
    val overlayTextColor: Int
        get() = Color.WHITE

    /**
     * The text size in the overlay, in density-independent pixels (dp).
     * Must be between 1 and 30 dp; resets to default and shows a toast if invalid.
     */
    val overlayTextSize: Int
        get() {
            val size = Settings.SWIPE_OVERLAY_TEXT_SIZE.get()
            if (size < 1 || size > 30) {
                Utils.showToastLong(str("revanced_swipe_text_overlay_size_invalid_toast"))
                return Settings.SWIPE_OVERLAY_TEXT_SIZE.resetToDefault()
            }
            return size
        }

    /**
     * Defines the style of the swipe controls overlay, determining its layout and appearance.
     *
     * @property isMinimal Indicates whether the style is minimalistic, omitting detailed progress indicators.
     * @property isHorizontalMinimalCenter Indicates whether the style is a minimal horizontal bar centered vertically.
     * @property isCircular Indicates whether the style uses a circular progress bar.
     * @property isVertical Indicates whether the style uses a vertical progress bar.
     */
    @Suppress("unused")
    enum class SwipeOverlayStyle(
        val isMinimal: Boolean = false,
        val isHorizontalMinimalCenter: Boolean = false,
        val isCircular: Boolean = false,
        val isVertical: Boolean = false
    ) {
        /**
         * A full horizontal progress bar with detailed indicators.
         */
        HORIZONTAL,

        /**
         * A minimal horizontal progress bar positioned at the top.
         */
        HORIZONTAL_MINIMAL_TOP(isMinimal = true),

        /**
         * A minimal horizontal progress bar centered vertically.
         */
        HORIZONTAL_MINIMAL_CENTER(isMinimal = true, isHorizontalMinimalCenter = true),

        /**
         * A full circular progress bar with detailed indicators.
         */
        CIRCULAR(isCircular = true),

        /**
         * A minimal circular progress bar.
         */
        CIRCULAR_MINIMAL(isMinimal = true, isCircular = true),

        /**
         * A full vertical progress bar with detailed indicators.
         */
        VERTICAL(isVertical = true),

        /**
         * A minimal vertical progress bar.
         */
        VERTICAL_MINIMAL(isMinimal = true, isVertical = true)
    }

    /**
     * The current style of the overlay, determining its layout and appearance.
     */
    val overlayStyle: SwipeOverlayStyle
        get() = Settings.SWIPE_OVERLAY_STYLE.get()
    //endregion

    //region behaviour
    /**
     * Indicates whether the brightness level should be saved and restored when entering or exiting fullscreen mode.
     */
    val shouldSaveAndRestoreBrightness: Boolean
        get() = Settings.SWIPE_SAVE_AND_RESTORE_BRIGHTNESS.get()

    /**
     * Indicates whether auto-brightness should be enabled when the brightness gesture reaches its lowest value.
     */
    val shouldLowestValueEnableAutoBrightness: Boolean
        get() = Settings.SWIPE_LOWEST_VALUE_ENABLE_AUTO_BRIGHTNESS.get()

    /**
     * The saved brightness value for the swipe gesture, used to restore brightness in fullscreen mode.
     */
    var savedScreenBrightnessValue: Float
        get() = Settings.SWIPE_BRIGHTNESS_VALUE.get()
        set(value) = Settings.SWIPE_BRIGHTNESS_VALUE.save(value)
    //endregion
}
