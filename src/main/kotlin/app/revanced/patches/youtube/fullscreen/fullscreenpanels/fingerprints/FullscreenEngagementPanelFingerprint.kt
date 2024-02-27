package app.revanced.patches.youtube.fullscreen.fullscreenpanels.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.FullScreenEngagementPanel
import app.revanced.util.fingerprint.LiteralValueFingerprint

object FullscreenEngagementPanelFingerprint : LiteralValueFingerprint(
    returnType = "L",
    parameters = listOf("L"),
    literalSupplier = { FullScreenEngagementPanel }
)