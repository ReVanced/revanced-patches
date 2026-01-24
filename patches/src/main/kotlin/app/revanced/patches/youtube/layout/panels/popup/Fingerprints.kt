package app.revanced.patches.youtube.layout.panels.popup

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.engagementPanelControllerMethod by gettingFirstMutableMethodDeclaratively(
    "EngagementPanelController: cannot show EngagementPanel before EngagementPanelController.init() has been called.",
    "[EngagementPanel] Cannot show EngagementPanel before EngagementPanelController.init() has been called.",
) {
    returnType("L")
}
