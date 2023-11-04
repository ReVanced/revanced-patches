package app.revanced.patches.youtube.misc.dimensions.spoof.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object DeviceDimensionsModelToStringFingerprint : MethodFingerprint(
    returnType = "L",
    strings = listOf("minh.", ";maxh.")
)
