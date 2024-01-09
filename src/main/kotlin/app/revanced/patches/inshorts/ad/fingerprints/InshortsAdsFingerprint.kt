package app.revanced.patches.inshorts.ad.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object InshortsAdsFingerprint : MethodFingerprint(
    "V",
    strings = listOf("GoogleAdLoader","exception in requestAd"),
)