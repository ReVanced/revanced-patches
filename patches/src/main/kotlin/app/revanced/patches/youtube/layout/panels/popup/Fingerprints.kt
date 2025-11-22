package app.revanced.patches.youtube.layout.panels.popup

import app.revanced.patcher.fingerprint

internal val engagementPanelControllerFingerprint = fingerprint {
    returns("L")
    strings(
        "EngagementPanelController: cannot show EngagementPanel before EngagementPanelController.init() has been called.",
        "[EngagementPanel] Cannot show EngagementPanel before EngagementPanelController.init() has been called.",
    )
}
