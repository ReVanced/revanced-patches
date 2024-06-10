package app.revanced.patches.youtube.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.video.speed.custom.speedUnavailableId
import app.revanced.util.literal

internal val showOldPlaybackSpeedMenuFingerprint = methodFingerprint {
    literal { speedUnavailableId }
}
