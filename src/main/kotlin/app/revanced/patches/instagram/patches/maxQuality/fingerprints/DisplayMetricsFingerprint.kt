package app.revanced.patches.instagram.patches.maxQuality.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object DisplayMetricsFingerprint : MethodFingerprint(
    strings = listOf("%sdpi; %sx%s"),
    returnType = "Ljava/lang/String;",
)
