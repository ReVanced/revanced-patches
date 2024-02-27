package app.revanced.patches.youtube.misc.spoofdimensions.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object DeviceDimensionsModelToStringFingerprint : MethodFingerprint(
    returnType = "L",
    strings = listOf("minh.", ";maxh.")
)
