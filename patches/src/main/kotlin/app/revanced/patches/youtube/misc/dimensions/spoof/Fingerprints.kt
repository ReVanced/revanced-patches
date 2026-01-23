package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.addString
import app.revanced.patcher.fingerprint

internal val deviceDimensionsModelToStringFingerprint = fingerprint {
    returnType("L")
    instructions(
        addString("minh."),
        addString(";maxh."),
    )
}
