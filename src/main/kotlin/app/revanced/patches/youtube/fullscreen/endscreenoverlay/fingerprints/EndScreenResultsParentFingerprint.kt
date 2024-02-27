package app.revanced.patches.youtube.fullscreen.endscreenoverlay.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.AppRelatedEndScreenResults
import app.revanced.util.fingerprint.LiteralValueFingerprint

object EndScreenResultsParentFingerprint : LiteralValueFingerprint(
    returnType = "V",
    literalSupplier = { AppRelatedEndScreenResults }
)