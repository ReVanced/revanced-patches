package app.revanced.patches.youtube.layout.playerpopuppanels.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags


@FuzzyPatternScanMethod(3)
object EngagementPanelControllerFingerprint : MethodFingerprint(
    returnType = "L",
    access = AccessFlags.PRIVATE or AccessFlags.FINAL,
    strings = listOf(
        "EngagementPanelController: cannot show EngagementPanel before EngagementPanelController.init() has been called.",
        "[EngagementPanel] Cannot show EngagementPanel before EngagementPanelController.init() has been called."
    )
)