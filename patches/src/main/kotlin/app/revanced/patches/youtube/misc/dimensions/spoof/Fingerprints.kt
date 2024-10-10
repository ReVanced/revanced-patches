package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.fingerprint

internal val deviceDimensionsModelToStringFingerprint = fingerprint {
    returns("L")
    strings("minh.", ";maxh.")
}
