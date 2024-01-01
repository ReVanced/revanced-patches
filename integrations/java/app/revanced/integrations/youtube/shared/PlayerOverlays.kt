package app.revanced.integrations.youtube.shared

import android.view.View
import android.view.ViewGroup
import app.revanced.integrations.youtube.swipecontrols.misc.Rectangle
import app.revanced.integrations.youtube.Event

/**
 * hooking class for player overlays
 */
@Suppress("MemberVisibilityCanBePrivate")
object PlayerOverlays {

    /**
     * called when the overlays finished inflating
     */
    val onInflate = Event<ViewGroup>()

    /**
     * called when new children are added or removed from the overlay
     */
    val onChildrenChange = Event<ChildrenChangeEventArgs>()

    /**
     * called when the overlay layout changes
     */
    val onLayoutChange = Event<LayoutChangeEventArgs>()

    /**
     * start listening for events on the provided view group
     *
     * @param overlaysLayout the overlays view group
     */
    @JvmStatic
    fun attach(overlaysLayout: ViewGroup) {
        onInflate.invoke(overlaysLayout)
        overlaysLayout.setOnHierarchyChangeListener(object :
            ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                if (parent is ViewGroup && child is View) {
                    onChildrenChange(
                        ChildrenChangeEventArgs(
                            parent,
                            child,
                            false
                        )
                    )
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {
                if (parent is ViewGroup && child is View) {
                    onChildrenChange(
                        ChildrenChangeEventArgs(
                            parent,
                            child,
                            true
                        )
                    )
                }
            }
        })
        overlaysLayout.addOnLayoutChangeListener { view, newLeft, newTop, newRight, newBottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (view is ViewGroup) {
                onLayoutChange(
                    LayoutChangeEventArgs(
                        view,
                        Rectangle(
                            oldLeft,
                            oldTop,
                            oldRight - oldLeft,
                            oldBottom - oldTop
                        ),
                        Rectangle(
                            newLeft,
                            newTop,
                            newRight - newLeft,
                            newBottom - newTop
                        )
                    )
                )
            }
        }
    }
}

data class ChildrenChangeEventArgs(
    val overlaysLayout: ViewGroup,
    val childView: View,
    val wasChildRemoved: Boolean
)

data class LayoutChangeEventArgs(
    val overlaysLayout: ViewGroup,
    val oldRect: Rectangle,
    val newRect: Rectangle
)
