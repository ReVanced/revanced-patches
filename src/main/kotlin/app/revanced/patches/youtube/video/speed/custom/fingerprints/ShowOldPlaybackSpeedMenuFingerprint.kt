package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patches.youtube.video.speed.custom.speedUnavailableId
import app.revanced.util.patch.literalValueFingerprint

internal val showOldPlaybackSpeedMenuFingerprint = literalValueFingerprint(literalSupplier = { speedUnavailableId })
