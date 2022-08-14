package app.revanced.integrations.swipecontrols

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import app.revanced.integrations.shared.PlayerType
import app.revanced.integrations.swipecontrols.controller.AudioVolumeController
import app.revanced.integrations.swipecontrols.controller.ScreenBrightnessController
import app.revanced.integrations.swipecontrols.controller.SwipeZonesController
import app.revanced.integrations.swipecontrols.controller.VolumeKeysController
import app.revanced.integrations.swipecontrols.controller.gesture.NoPtSSwipeGestureController
import app.revanced.integrations.swipecontrols.controller.gesture.SwipeGestureController
import app.revanced.integrations.swipecontrols.misc.Rectangle
import app.revanced.integrations.swipecontrols.views.SwipeControlsOverlayLayout
import app.revanced.integrations.utils.LogHelper
import java.lang.ref.WeakReference

/**
 * The main controller for volume and brightness swipe controls.
 * note that the superclass is overwritten to the superclass of the WatchWhileActivity at patch time
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
    private lateinit var gesture: SwipeGestureController

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

        // create controllers
        LogHelper.info(this.javaClass, "initializing swipe controls controllers")
        config = SwipeControlsConfigurationProvider(this)
        gesture = createGestureController()
        keys = VolumeKeysController(this)
        audio = createAudioController()
        screen = createScreenController()

        // create overlay
        SwipeControlsOverlayLayout(this).let {
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

        // listen for changes in the player type
        PlayerType.onChange += this::onPlayerTypeChanged

        // set current instance reference
        currentHost = WeakReference(this)
    }

    override fun onStart() {
        super.onStart()

        // (re) attach overlay
        LogHelper.info(this.javaClass, "attaching swipe controls overlay")
        contentRoot.removeView(overlay)
        contentRoot.addView(overlay)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if ((ev != null) && gesture.onTouchEvent(ev)) true else {
            super.dispatchTouchEvent(ev)
        }
    }

    override fun dispatchKeyEvent(ev: KeyEvent?): Boolean {
        return if((ev != null) && keys.onKeyEvent(ev)) true else {
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
     * called when the player type changes
     *
     * @param type the new player type
     */
    private fun onPlayerTypeChanged(type: PlayerType) {
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
            SwipeGestureController(this)
        else NoPtSSwipeGestureController(this)

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
