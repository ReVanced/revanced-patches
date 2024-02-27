package app.revanced.patches.shared.fingerprints.ads

import app.revanced.patcher.fingerprint.MethodFingerprint

object MainstreamAdsFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("markFillRequested", "requestEnterSlot")
)