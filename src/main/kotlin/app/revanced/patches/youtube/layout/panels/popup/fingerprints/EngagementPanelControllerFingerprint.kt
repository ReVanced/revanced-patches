package app.revanced.patches.youtube.layout.panels.popup.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val engagementPanelControllerFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("L")
    strings(
        "EngagementPanelController: cannot show EngagementPanel before EngagementPanelController.init() has been called.",
        "[EngagementPanel] Cannot show EngagementPanel before EngagementPanelController.init() has been called.",
    )
}
