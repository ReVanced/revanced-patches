package app.revanced.patches.youtube.misc.tracking.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

/**
 * Sharing panel of System
 */
object SystemShareLinkFormatterFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L", "Ljava/util/Map;"),
    strings = listOf("YTShare_Logging_Share_Intent_Endpoint_Byte_Array")
)