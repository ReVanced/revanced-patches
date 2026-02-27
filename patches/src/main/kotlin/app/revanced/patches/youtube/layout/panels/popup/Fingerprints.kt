package app.revanced.patches.youtube.layout.panels.popup

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.engagementPanelControllerMethod by gettingFirstMethodDeclaratively(
    "EngagementPanelController: cannot show EngagementPanel before EngagementPanelController.init() has been called.",
    "[EngagementPanel] Cannot show EngagementPanel before EngagementPanelController.init() has been called.",
) {
    returnType("L")
}
