package app.revanced.integrations.swipecontrols.controller

import android.content.Context
import android.util.TypedValue
import android.view.View
import app.revanced.integrations.shared.LayoutChangeEventArgs
import app.revanced.integrations.shared.PlayerOverlays
import app.revanced.integrations.swipecontrols.misc.Rectangle
import app.revanced.integrations.swipecontrols.misc.applyDimension
import app.revanced.integrations.utils.ReVancedUtils

/**
 * Y- Axis:
 * -------- 0
 *        ^
 * dead   | 40dp
 *        v
 * -------- yDeadTop
 *        ^
 * swipe  |
 *        v
 * -------- yDeadBtm
 *        ^
 * dead   | 80dp
 *        v
 * -------- screenHeight
 *
 * X- Axis:
 *  0    xBrigStart    xBrigEnd    xVolStart     xVolEnd   screenWidth
 *  |          |            |          |            |          |
 *  |   20dp   |    3/8     |    2/8   |    3/8     |   20dp   |
 *  | <------> |  <------>  | <------> |  <------>  | <------> |
 *  |   dead   | brightness |   dead   |   volume   |   dead   |
 *             | <--------------------------------> |
 *                              1/1
 */
@Suppress("PrivatePropertyName")
class SwipeZonesController(
    context: Context,
    private val fallbackScreenRect: () -> Rectangle
) {
    /**
     * 20dp, in pixels
     */
    private val _20dp = 20.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)

    /**
     * 40dp, in pixels
     */
    private val _40dp = 40.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)

    /**
     * 80dp, in pixels
     */
    private val _80dp = 80.applyDimension(context, TypedValue.COMPLEX_UNIT_DIP)

    /**
     * id for R.id.engagement_panel
     */
    private val engagementPanelId =
        ReVancedUtils.getResourceIdByName(context, "id", "engagement_panel")

    /**
     * current bounding rectangle of the player overlays
     */
    private var playerRect: Rectangle? = null

    /**
     * current bounding rectangle of the engagement_panel
     */
    private var engagementPanelRect = Rectangle(0, 0, 0, 0)

    /**
     * listener for player overlays layout change
     */
    private fun onOverlaysLayoutChanged(args: LayoutChangeEventArgs) {
        // update engagement panel bounds
        val engagementPanel = args.overlaysLayout.findViewById<View>(engagementPanelId)
        engagementPanelRect =
            if (engagementPanel == null || engagementPanel.visibility != View.VISIBLE) {
                Rectangle(0, 0, 0, 0)
            } else {
                Rectangle(
                    engagementPanel.x.toInt(),
                    engagementPanel.y.toInt(),
                    engagementPanel.width,
                    engagementPanel.height
                )
            }

        // update player bounds
        playerRect = args.newRect
    }

    init {
        PlayerOverlays.onLayoutChange += this::onOverlaysLayoutChanged
    }

    /**
     * rectangle of the area that is effectively usable for swipe controls
     */
    private val effectiveSwipeRect: Rectangle
        get() {
            val p = if (playerRect != null) playerRect!! else fallbackScreenRect()
            return Rectangle(
                p.x + _20dp,
                p.y + _40dp,
                p.width - engagementPanelRect.width - _20dp,
                p.height - _20dp - _80dp
            )
        }

    /**
     * the rectangle of the volume control zone
     */
    val volume: Rectangle
        get() {
            val zoneWidth = (effectiveSwipeRect.width * 3) / 8
            return Rectangle(
                effectiveSwipeRect.right - zoneWidth,
                effectiveSwipeRect.top,
                zoneWidth,
                effectiveSwipeRect.height
            )
        }

    /**
     * the rectangle of the screen brightness control zone
     */
    val brightness: Rectangle
        get() {
            val zoneWidth = (effectiveSwipeRect.width * 3) / 8
            return Rectangle(
                effectiveSwipeRect.left,
                effectiveSwipeRect.top,
                zoneWidth,
                effectiveSwipeRect.height
            )
        }
}