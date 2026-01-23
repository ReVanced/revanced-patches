package app.revanced.patches.youtube.layout.panels.popup

internal val BytecodePatchContext.engagementPanelControllerMethod by gettingFirstMethodDeclaratively {
    returnType("L")
    strings(
        "EngagementPanelController: cannot show EngagementPanel before EngagementPanelController.init() has been called.",
        "[EngagementPanel] Cannot show EngagementPanel before EngagementPanelController.init() has been called.",
    )
}
