package app.revanced.patches.youtube.misc.dimensions.spoof.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val deviceDimensionsModelToStringFingerprint = methodFingerprint {
    returns("L")
    strings("minh.", ";maxh.")
}
