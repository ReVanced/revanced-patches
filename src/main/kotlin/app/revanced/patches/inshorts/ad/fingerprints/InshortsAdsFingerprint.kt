package app.revanced.patches.inshorts.ad.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object InshortsAdsFingerprint : MethodFingerprint(
    "V",
    strings = listOf("GoogleAdLoader","exception in requestAd"),
)