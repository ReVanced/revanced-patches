package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.fingerprint
import app.revanced.patcher.addString

internal val deviceDimensionsModelToStringFingerprint = fingerprint {
    returns("L")
    instructions(
        addString("minh."),
        addString(";maxh.")
    )
}
