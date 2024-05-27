package app.revanced.patches.youtube.video.information.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val playerInitFingerprint = methodFingerprint {
    strings("playVideo called on player response with no videoStreamingData.")
}
