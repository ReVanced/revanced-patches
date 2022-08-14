package app.revanced.integrations.swipecontrols

import android.content.Context
import android.graphics.Color
import app.revanced.integrations.settings.SettingsEnum
import app.revanced.integrations.shared.PlayerType

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
        get() = SettingsEnum.ENABLE_SWIPE_VOLUME.boolean

    /**
     * should swipe controls for volume be enabled?
     */
    val enableBrightnessControl: Boolean
        get() = SettingsEnum.ENABLE_SWIPE_BRIGHTNESS.boolean

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
//endregioin

//region gesture adjustments
    /**
     * should press-to-swipe be enabled?
     */
    val shouldEnablePressToSwipe: Boolean
        get() = SettingsEnum.ENABLE_PRESS_TO_SWIPE.boolean

    /**
     * threshold for swipe detection
     * this may be called rapidly in onScroll, so we have to load it once and then leave it constant
     */
    val swipeMagnitudeThreshold: Float
        get() = SettingsEnum.SWIPE_MAGNITUDE_THRESHOLD.float
//endregion

//region overlay adjustments

    /**
     * should the overlay enable haptic feedback?
     */
    val shouldEnableHapticFeedback: Boolean
        get() = SettingsEnum.ENABLE_SWIPE_HAPTIC_FEEDBACK.boolean

    /**
     * how long the overlay should be shown on changes
     */
    val overlayShowTimeoutMillis: Long
        get() = SettingsEnum.SWIPE_OVERLAY_TIMEOUT.long

    /**
     * text size for the overlay, in sp
     */
    val overlayTextSize: Float
        get() = SettingsEnum.SWIPE_OVERLAY_TEXT_SIZE.float

    /**
     * get the background color for text on the overlay, as a color int
     */
    val overlayTextBackgroundColor: Int
        get() = Color.argb(SettingsEnum.SWIPE_OVERLAY_BACKGROUND_ALPHA.int, 0, 0, 0)

    /**
     * get the foreground color for text on the overlay, as a color int
     */
    val overlayForegroundColor: Int
        get() = Color.WHITE

//endregion
}