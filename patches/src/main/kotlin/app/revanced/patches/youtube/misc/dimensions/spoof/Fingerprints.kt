package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string

internal val deviceDimensionsModelToStringFingerprint by fingerprint {
    returns("L")
    instructions(
        string("minh."),
        string(";maxh.")
    )
}
