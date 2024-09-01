package app.revanced.patches.instagram.misc.maxQuality.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object VideoEncoderConfigFingerprint:MethodFingerprint(
    strings = listOf("VideoEncoderConfig{width="),
    returnType = "Ljava/lang/String;"
)