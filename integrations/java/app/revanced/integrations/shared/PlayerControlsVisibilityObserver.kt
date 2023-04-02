package app.revanced.integrations.shared

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import app.revanced.integrations.utils.ReVancedUtils
import java.lang.ref.WeakReference

/**
 * default implementation of [PlayerControlsVisibilityObserver]
 *
 * @param activity activity that contains the controls_layout view
 */
class PlayerControlsVisibilityObserverImpl(
    private val activity: Activity
) : PlayerControlsVisibilityObserver {

    /**
     * id of the direct parent of controls_layout, R.id.youtube_controls_overlay
     */
    private val controlsLayoutParentId =
        ReVancedUtils.getResourceIdentifier(activity, "youtube_controls_overlay", "id")

    /**
     * id of R.id.controls_layout
     */
    private val controlsLayoutId =
        ReVancedUtils.getResourceIdentifier(activity, "controls_layout", "id")

    /**
     * reference to the controls layout view
     */
    private var controlsLayoutView = WeakReference<View>(null)

    /**
     * is the [controlsLayoutView] set to a valid reference of a view?
     */
    private val isAttached: Boolean
        get() {
            val view = controlsLayoutView.get()
            return view != null && view.parent != null
        }

    /**
     * find and attach the controls_layout view if needed
     */
    private fun maybeAttach() {
        if (isAttached) return

        // find parent, then controls_layout view
        // this is needed because there may be two views where id=R.id.controls_layout
        // because why should google confine themselves to their own guidelines...
        activity.findViewById<ViewGroup>(controlsLayoutParentId)?.let { parent ->
            parent.findViewById<View>(controlsLayoutId)?.let {
                controlsLayoutView = WeakReference(it)
            }
        }
    }

    override val playerControlsVisibility: Int
        get() {
            maybeAttach()
            return controlsLayoutView.get()?.visibility ?: View.GONE
        }

    override val arePlayerControlsVisible: Boolean
        get() = playerControlsVisibility == View.VISIBLE
}

/**
 * provides the visibility status of the fullscreen player controls_layout view.
 * this can be used for detecting when the player controls are shown
 */
interface PlayerControlsVisibilityObserver {
    /**
     * current visibility int of the controls_layout view
     */
    val playerControlsVisibility: Int

    /**
     * is the value of [playerControlsVisibility] equal to [View.VISIBLE]?
     */
    val arePlayerControlsVisible: Boolean
}