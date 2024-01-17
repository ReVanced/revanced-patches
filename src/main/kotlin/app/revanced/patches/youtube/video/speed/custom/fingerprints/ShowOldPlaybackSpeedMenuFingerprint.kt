package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patches.youtube.video.speed.custom.CustomPlaybackSpeedResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint

internal object ShowOldPlaybackSpeedMenuFingerprint : LiteralValueFingerprint(
    literalSupplier = {
        CustomPlaybackSpeedResourcePatch.speedUnavailableId
    }
)
