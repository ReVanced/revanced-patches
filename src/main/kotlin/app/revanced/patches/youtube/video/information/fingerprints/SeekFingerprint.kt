package app.revanced.patches.youtube.video.information.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val seekFingerprint = methodFingerprint {
    strings("Attempting to seek during an ad")
}
