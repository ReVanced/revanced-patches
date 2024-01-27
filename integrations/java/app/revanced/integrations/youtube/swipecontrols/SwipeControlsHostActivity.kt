package app.revanced.integrations.youtube.swipecontrols

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import app.revanced.integrations.youtube.shared.PlayerType
import app.revanced.integrations.youtube.swipecontrols.controller.AudioVolumeController
import app.revanced.integrations.youtube.swipecontrols.controller.ScreenBrightnessController
import app.revanced.integrations.youtube.swipecontrols.controller.SwipeZonesController
import app.revanced.integrations.youtube.swipecontrols.controller.VolumeKeysController
import app.revanced.integrations.youtube.swipecontrols.controller.gesture.ClassicSwipeController
import app.revanced.integrations.youtube.swipecontrols.controller.gesture.PressToSwipeController
import app.revanced.integrations.youtube.swipecontrols.controller.gesture.core.GestureController
import app.revanced.integrations.youtube.swipecontrols.misc.Rectangle
import app.revanced.integrations.youtube.swipecontrols.views.SwipeControlsOverlayLayout
import app.revanced.integrations.shared.Logger.printDebug
import app.revanced.integrations.shared.Logger.printException
import java.lang.ref.WeakReference

/**
 * The main controller for volume and brightness swipe controls.
 * note that the superclass is overwritten to the superclass of the MainActivity at patch time
 *
 * @smali Lapp/revanced/integrations/swipecontrols/SwipeControlsHostActivity;
 */
class SwipeControlsHostActivity : Activity() {
    /**
     * current instance of [AudioVolumeController]
     */
    var audio: AudioVolumeController? = null

    /**
     * current instance of [ScreenBrightnessController]
     */
    var screen: ScreenBrightnessController? = null

    /**
     * current instance of [SwipeControlsConfigurationProvider]
     */
    lateinit var config: SwipeControlsConfigurationProvider

    /**
     * current instance of [SwipeControlsOverlayLayout]
     */
    lateinit var overlay: SwipeControlsOverlayLayout

    /**
     * current instance of [SwipeZonesController]
     */
    lateinit var zones: SwipeZonesController

    /**
     * main gesture controller
     */
    private lateinit var gesture: GestureController

    /**
     * main volume keys controller
     */
    private lateinit var keys: VolumeKeysController

    /**
     * current content view with id [android.R.id.content]
     */
    private val contentRoot
        get() = window.decorView.findViewById<ViewGroup>(android.R.id.content)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    override fun onStart() {
        super.onStart()
        reAttachOverlays()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ensureInitialized()
        return if ((ev != null) && gesture.submitTouchEvent(ev)) true else {
            super.dispatchTouchEvent(ev)
        }
    }

    override fun dispatchKeyEvent(ev: KeyEvent?): Boolean {
        ensureInitialized()
        return if ((ev != null) && keys.onKeyEvent(ev)) true else {
            super.dispatchKeyEvent(ev)
        }
    }

    /**
     * dispatch a touch event to downstream views
     *
     * @param event the event to dispatch
     * @return was the event consumed?
     */
    fun dispatchDownstreamTouchEvent(event: MotionEvent) =
        super.dispatchTouchEvent(event)

    /**
     * ensures that swipe controllers are initialized and attached.
     * on some ROMs with SDK <= 23, [onCreate] and [onStart] may not be called correctly.
     * see https://github.com/revanced/revanced-patches/issues/446
     */
    private fun ensureInitialized() {
        if (!this::config.isInitialized) {
            printException {
                "swipe controls were not initialized in onCreate, initializing on-the-fly (SDK is ${Build.VERSION.SDK_INT})"
            }
            initialize()
            reAttachOverlays()
        }
    }

    /**
     * initializes controllers, only call once
     */
    private fun initialize() {
        // create controllers
        printDebug { "initializing swipe controls controllers" }
        config = SwipeControlsConfigurationProvider(this)
        keys = VolumeKeysController(this)
        audio = createAudioController()
        screen = createScreenController()

        // create overlay
        SwipeControlsOverlayLayout(this, config).let {
            overlay = it
            contentRoot.addView(it)
        }

        // create swipe zone controller
        zones = SwipeZonesController(this) {
            Rectangle(
                contentRoot.x.toInt(),
                contentRoot.y.toInt(),
                contentRoot.width,
                contentRoot.height
            )
        }

        // create the gesture controller
        gesture = createGestureController()

        // listen for changes in the player type
        PlayerType.onChange += this::onPlayerTypeChanged

        // set current instance reference
        currentHost = WeakReference(this)
    }

    /**
     * (re) attaches swipe overlays
     */
    private fun reAttachOverlays() {
        printDebug{ "attaching swipe controls overlay" }
        contentRoot.removeView(overlay)
        contentRoot.addView(overlay)
    }

    /**
     * called when the player type changes
     *
     * @param type the new player type
     */
    private fun onPlayerTypeChanged(type: PlayerType) {
        if (config.shouldSaveAndRestoreBrightness)
            when (type) {
                PlayerType.WATCH_WHILE_FULLSCREEN -> screen?.restore()
                else -> {
                    screen?.save()
                    screen?.restoreDefaultBrightness()
                }
            }
    }

    /**
     * create the audio volume controller
     */
    private fun createAudioController() =
        if (config.enableVolumeControls)
            AudioVolumeController(this) else null

    /**
     * create the screen brightness controller instance
     */
    private fun createScreenController() =
        if (config.enableBrightnessControl)
            ScreenBrightnessController(this) else null

    /**
     * create the gesture controller based on settings
     */
    private fun createGestureController() =
        if (config.shouldEnablePressToSwipe)
            PressToSwipeController(this)
        else ClassicSwipeController(this)

    companion object {
        /**
         * the currently active swipe controls host.
         * the reference may be null!
         */
        @JvmStatic
        var currentHost: WeakReference<SwipeControlsHostActivity> = WeakReference(null)
            private set
    }
}
