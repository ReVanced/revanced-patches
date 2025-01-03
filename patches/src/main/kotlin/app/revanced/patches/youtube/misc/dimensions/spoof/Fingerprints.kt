package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.fingerprint

internal val deviceDimensionsModelToStringFingerprint by fingerprint {
    returns("L")
    strings("minh.", ";maxh.")
}
