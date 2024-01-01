package app.revanced.integrations.youtube.swipecontrols

import android.content.Context
import android.graphics.Color
import app.revanced.integrations.youtube.settings.Settings
import app.revanced.integrations.youtube.shared.PlayerType

/**
 * provider for configuration for volume and brightness swipe controls
 *
 * @param context the context to create in
 */
class SwipeControlsConfigurationProvider(
        private val context: Context
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
        get() = Settings.SWIPE_OVERLAY_TEXT_SIZE.get()

    /**
     * get the background color for text on the overlay, as a color int
     */
    val overlayTextBackgroundColor: Int
        get() = Color.argb(Settings.SWIPE_OVERLAY_BACKGROUND_ALPHA.get(), 0, 0, 0)

    /**
     * get the foreground color for text on the overlay, as a color int
     */
    val overlayForegroundColor: Int
        get() = Color.WHITE

//endregion

//region behaviour

    /**
     * should the brightness be saved and restored when exiting or entering fullscreen
     */
    val shouldSaveAndRestoreBrightness: Boolean
        get() = Settings.SWIPE_SAVE_AND_RESTORE_BRIGHTNESS.get()

//endregion
}